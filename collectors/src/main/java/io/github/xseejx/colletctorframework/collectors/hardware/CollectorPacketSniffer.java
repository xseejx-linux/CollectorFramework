package io.github.xseejx.colletctorframework.collectors.hardware;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.auto.service.AutoService;
import io.github.xseejx.colletctorframework.core.api.Collector;
import io.github.xseejx.colletctorframework.core.api.CollectorMetadata;
import io.github.xseejx.colletctorframework.core.api.CollectorResult;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.pcap4j.core.*;
import org.pcap4j.core.BpfProgram.BpfCompileMode;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.packet.*;
import org.pcap4j.packet.IpV4Packet.IpV4Header;

/**
 * ⚠️  SOLO PER USO DIFENSIVO / DIAGNOSTICO / CYBERSECURITY LEARNING
 *    Non usare per intercettare traffico di terze parti senza autorizzazione.
 *
 * Packet Sniffer Collector — cattura pacchetti in tempo reale via Pcap4J.
 *
 * PREREQUISITI DI SISTEMA:
 *   Windows : installa Npcap (https://npcap.com) — esegui il framework come Administrator
 *   Linux   : installa libpcap-dev, poi: sudo setcap cap_net_raw,cap_net_admin=eip $(which java)
 *             oppure esegui come root
 *   macOS   : libpcap è già presente; esegui come root o con sudo
 *
 * DIPENDENZE MAVEN (aggiungere al pom.xml):
 * ─────────────────────────────────────────
 *   <dependency>
 *     <groupId>org.pcap4j</groupId>
 *     <artifactId>pcap4j-core</artifactId>
 *     <version>1.8.2</version>
 *   </dependency>
 *   <dependency>
 *     <groupId>org.pcap4j</groupId>
 *     <artifactId>pcap4j-packetfactory-static</artifactId>
 *     <version>1.8.2</version>
 *   </dependency>
 *   <!-- Pcap4J usa JNA internamente — già incluso come transitiva -->
 *
 * DIPENDENZE GRADLE:
 * ──────────────────
 *   implementation 'org.pcap4j:pcap4j-core:1.8.2'
 *   implementation 'org.pcap4j:pcap4j-packetfactory-static:1.8.2'
 *
 * Parametri via reflection:
 *   captureSeconds   → durata cattura in secondi (default: 5)
 *   maxPackets       → massimo pacchetti da registrare nel JSON (default: 100)
 *   bpfFilter        → filtro BPF personalizzato (default: "ip", solo IPv4)
 *   includeAll       → include anche pacchetti non-IP nel conteggio
 */
@AutoService(Collector.class)
@CollectorMetadata(
    name        = "hardware.packetsniffer",
    description = "Packet sniffing difensivo in tempo reale via Pcap4J: analisi TCP/UDP/ICMP e alert di sicurezza",
    tags        = {"hardware", "network", "security", "realtime"}
)
public class CollectorPacketSniffer implements Collector {
    private static final Logger logger = LoggerFactory.getLogger(CollectorPacketSniffer.class);

    // ── Parametri via reflection ──────────────────────────────────────────────
    int     captureSeconds = 5;
    int     maxPackets     = 100;
    String  bpfFilter      = "ip";      // filtro BPF: "ip", "tcp", "udp port 53", ecc.
    boolean includeAll     = false;

    // ── Costanti ──────────────────────────────────────────────────────────────
    private static final int    SNAP_LEN         = 65536;
    private static final int    READ_TIMEOUT_MS  = 50;

    // ── Soglie alert ──────────────────────────────────────────────────────────
    /** Pacchetti totali oltre cui scatta alert flood. */
    private static final int FLOOD_THRESHOLD        = 500;
    /** Connessioni SYN verso host diversi per rilevare port scan. */
    private static final int PORT_SCAN_THRESHOLD    = 20;
    /** Pacchetti verso la stessa porta oltre cui scatta alert. */
    private static final int PORT_FLOOD_THRESHOLD   = 50;
    /** Pacchetti ICMP oltre cui sono anomali. */
    private static final int ICMP_FLOOD_THRESHOLD   = 30;

    private static final SimpleDateFormat TS_FMT =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public String getName() { return "hardware.packetsniffer"; }

    @Override
    @SuppressWarnings("unchecked")
    public CollectorResult collect() {
        JSONObject result = new JSONObject();

        // ── Selezione interfaccia ─────────────────────────────────────────────
        PcapNetworkInterface nif;
        try {
            nif = selectInterface();
        } catch (Exception e) {
            logger.error("Cannot enumerate network interfaces: {}", e.getMessage());
            result.put("error", "Cannot enumerate interfaces: " + e.getMessage());
            result.put("hint",
                "Windows: installa Npcap e avvia come Administrator. " +
                "Linux: sudo setcap cap_net_raw,cap_net_admin=eip $(which java)");
            return CollectorResult.failure(getName(), result);
        }

        if (nif == null) {
            result.put("error", "Nessuna interfaccia di rete attiva trovata");
            return CollectorResult.failure(getName(), result);
        }

        logger.info("Packet sniffer avviato su interfaccia: {} ({})",
            nif.getName(), nif.getDescription());

        // ── Strutture dati condivise (thread-safe) ────────────────────────────
        JSONArray  packetArray    = new JSONArray();
        JSONArray  alertArray     = new JSONArray();

        AtomicInteger totalCount  = new AtomicInteger(0);
        AtomicInteger tcpCount    = new AtomicInteger(0);
        AtomicInteger udpCount    = new AtomicInteger(0);
        AtomicInteger icmpCount   = new AtomicInteger(0);

        // Per rilevamento anomalie
        Map<String, AtomicInteger> dstPortCount = new ConcurrentHashMap<>();  // porta dst → count
        Map<String, AtomicInteger> synSrcCount  = new ConcurrentHashMap<>();  // src IP → SYN count
        Map<String, Set<Integer>>  synDstPorts  = new ConcurrentHashMap<>();  // src IP → set porte

        // ── Apertura handle Pcap ──────────────────────────────────────────────
        PcapHandle handle;
        try {
            handle = nif.openLive(SNAP_LEN, PromiscuousMode.PROMISCUOUS, READ_TIMEOUT_MS);
        } catch (PcapNativeException e) {
            logger.error("Cannot open pcap handle: {}", e.getMessage());
            result.put("error", "Cannot open pcap handle: " + e.getMessage());
            return CollectorResult.failure(getName(), result);
        }

        // ── Applicazione filtro BPF ───────────────────────────────────────────
        try {
            if (bpfFilter != null && !bpfFilter.isBlank()) {
                handle.setFilter(bpfFilter, BpfCompileMode.OPTIMIZE);
                logger.debug("BPF filter applicato: '{}'", bpfFilter);
            }
        } catch (PcapNativeException | NotOpenException e) {
            logger.warn("BPF filter '{}' non applicabile: {}", bpfFilter, e.getMessage());
        }

        // ── Listener pacchetti ────────────────────────────────────────────────
        PacketListener listener = packet -> {
            totalCount.incrementAndGet();

            IpV4Packet ipPkt = packet.get(IpV4Packet.class);
            if (ipPkt == null) return;  // non-IPv4

            IpV4Header ipHeader = ipPkt.getHeader();
            String srcIp  = ipHeader.getSrcAddr().getHostAddress();
            String dstIp  = ipHeader.getDstAddr().getHostAddress();
            int    size   = packet.length();
            String ts     = TS_FMT.format(new Timestamp(System.currentTimeMillis()));

            String  proto   = "OTHER";
            int     srcPort = -1;
            int     dstPort = -1;
            boolean isSyn   = false;

            // ── Analisi layer trasporto ───────────────────────────────────────
            TcpPacket tcpPkt = packet.get(TcpPacket.class);
            if (tcpPkt != null) {
                proto   = "TCP";
                srcPort = tcpPkt.getHeader().getSrcPort().valueAsInt();
                dstPort = tcpPkt.getHeader().getDstPort().valueAsInt();
                isSyn   = tcpPkt.getHeader().getSyn() && !tcpPkt.getHeader().getAck();
                tcpCount.incrementAndGet();
            }

            UdpPacket udpPkt = packet.get(UdpPacket.class);
            if (udpPkt != null) {
                proto   = "UDP";
                srcPort = udpPkt.getHeader().getSrcPort().valueAsInt();
                dstPort = udpPkt.getHeader().getDstPort().valueAsInt();
                udpCount.incrementAndGet();
            }

            IcmpV4CommonPacket icmpPkt = packet.get(IcmpV4CommonPacket.class);
            if (icmpPkt != null) {
                proto = "ICMP";
                icmpCount.incrementAndGet();
            }

            // ── Raccolta dati per alert ───────────────────────────────────────
            if (dstPort > 0) {
                dstPortCount.computeIfAbsent(
                    proto + ":" + dstPort, k -> new AtomicInteger()).incrementAndGet();
            }

            // SYN tracking per port scan detection
            if (isSyn && dstPort > 0) {
                synSrcCount.computeIfAbsent(srcIp, k -> new AtomicInteger()).incrementAndGet();
                synDstPorts.computeIfAbsent(srcIp, k -> ConcurrentHashMap.newKeySet())
                           .add(dstPort);
            }

            // ── Aggiunta pacchetto al JSON (limite maxPackets) ────────────────
            if (packetArray.size() < maxPackets) {
                JSONObject p = new JSONObject();
                p.put("sourceIp",        srcIp);
                p.put("destinationIp",   dstIp);
                p.put("protocol",        proto);
                p.put("sourcePort",      srcPort > 0 ? srcPort : "N/A");
                p.put("destinationPort", dstPort > 0 ? dstPort : "N/A");
                p.put("size",            size);
                p.put("timestamp",       ts);
                p.put("direction",       isPublicIp(dstIp) ? "OUTBOUND" : "LOCAL");
                synchronized (packetArray) {
                    packetArray.add(p);
                }
            }
        };

        // ── Cattura in thread non bloccante ───────────────────────────────────
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> captureFuture = executor.submit(() -> {
            try {
                // -1 = loop infinito, controllato dal breakloop() dopo il timeout
                handle.loop(-1, listener);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (PcapNativeException | NotOpenException e) {
                logger.error("Errore durante la cattura: {}", e.getMessage());
            }
        });

        // Attende captureSeconds poi interrompe il loop
        try {
            Thread.sleep((long) captureSeconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        try {
            handle.breakLoop();
        } catch (NotOpenException e) {
            logger.warn("Handle già chiuso: {}", e.getMessage());
        }

        // Attende terminazione del thread di cattura (max 3s)
        try {
            captureFuture.get(3, TimeUnit.SECONDS);
        } catch (Exception e) {
            captureFuture.cancel(true);
        }

        handle.close();
        executor.shutdownNow();

        // ════════════════════════════════════════════════════════════════════
        // GENERAZIONE ALERT
        // ════════════════════════════════════════════════════════════════════

        // [FLOOD] Troppi pacchetti totali
        if (totalCount.get() > FLOOD_THRESHOLD) {
            alertArray.add(String.format(
                "[FLOOD] Flood di pacchetti rilevato: %d pacchetti in %ds (soglia: %d) — possibile DoS o traffico anomalo",
                totalCount.get(), captureSeconds, FLOOD_THRESHOLD));
        }

        // [PORT] Troppe connessioni verso la stessa porta
        dstPortCount.forEach((protoPort, count) -> {
            if (count.get() >= PORT_FLOOD_THRESHOLD) {
                alertArray.add(String.format(
                    "[PORT] Concentrazione di traffico su %s — %d pacchetti in %ds",
                    protoPort, count.get(), captureSeconds));
            }
        });

        // [SCAN] Port scan: un IP che apre SYN verso molte porte diverse
        synDstPorts.forEach((srcIp, ports) -> {
            if (ports.size() >= PORT_SCAN_THRESHOLD) {
                alertArray.add(String.format(
                    "[SCAN] Possibile port scan da %s: SYN verso %d porte distinte in %ds",
                    srcIp, ports.size(), captureSeconds));
            }
        });

        // [SYN] SYN flood: moltissimi SYN da uno stesso IP
        synSrcCount.forEach((srcIp, count) -> {
            if (count.get() >= PORT_FLOOD_THRESHOLD) {
                alertArray.add(String.format(
                    "[SYN] Possibile SYN flood da %s: %d pacchetti SYN in %ds",
                    srcIp, count.get(), captureSeconds));
            }
        });

        // [ICMP] Flood ICMP
        if (icmpCount.get() > ICMP_FLOOD_THRESHOLD) {
            alertArray.add(String.format(
                "[ICMP] Traffico ICMP anomalo: %d pacchetti in %ds (soglia: %d) — possibile ping flood o ricognizione",
                icmpCount.get(), captureSeconds, ICMP_FLOOD_THRESHOLD));
        }

        // ── Output finale ─────────────────────────────────────────────────────
        result.put("interface",    nif.getName() + " (" +
                                   (nif.getDescription() != null ? nif.getDescription() : "N/A") + ")");
        result.put("capturedSec",  captureSeconds);
        result.put("bpfFilter",    bpfFilter != null ? bpfFilter : "none");
        result.put("packetCount",  totalCount.get());
        result.put("tcpPackets",   tcpCount.get());
        result.put("udpPackets",   udpCount.get());
        result.put("icmpPackets",  icmpCount.get());
        result.put("packets",      packetArray);
        result.put("alertCount",   alertArray.size());
        result.put("alerts",       alertArray);

        return CollectorResult.ok(getName(), result);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Selezione interfaccia
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Seleziona automaticamente la prima interfaccia non-loopback con un
     * indirizzo IPv4 assegnato. Preferisce interfacce con traffico attivo
     * (bytes > 0), ma accetta qualsiasi interfaccia IPv4 come fallback.
     */
    private PcapNetworkInterface selectInterface() throws PcapNativeException {
        List<PcapNetworkInterface> allDevs = Pcaps.findAllDevs();
        if (allDevs == null || allDevs.isEmpty()) {
            throw new PcapNativeException("Nessuna interfaccia trovata — " +
                "verificare che Npcap/WinPcap (Windows) o libpcap (Linux/macOS) sia installato");
        }

        logger.debug("Interfacce disponibili:");
        allDevs.forEach(d -> logger.debug("  {} — {}", d.getName(), d.getDescription()));

        // Prima scelta: non-loopback con indirizzo IPv4
        for (PcapNetworkInterface nif : allDevs) {
            if (nif.isLoopBack()) continue;
            if (hasIpv4Address(nif)) {
                logger.info("Interfaccia selezionata: {}", nif.getName());
                return nif;
            }
        }

        // Fallback: qualsiasi interfaccia (incluso loopback)
        for (PcapNetworkInterface nif : allDevs) {
            if (hasIpv4Address(nif)) return nif;
        }

        // Ultimo fallback: prima disponibile
        return allDevs.get(0);
    }

    private boolean hasIpv4Address(PcapNetworkInterface nif) {
        if (nif.getAddresses() == null) return false;
        return nif.getAddresses().stream()
            .anyMatch(a -> a.getAddress() instanceof java.net.Inet4Address);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Helper
    // ════════════════════════════════════════════════════════════════════════

    /** Restituisce true se l'IP è pubblico (non RFC1918, non loopback). */
    private boolean isPublicIp(String ip) {
        if (ip == null) return false;
        if (ip.startsWith("127."))    return false;
        if (ip.startsWith("10."))     return false;
        if (ip.startsWith("192.168.")) return false;
        if (ip.startsWith("169.254.")) return false;
        if (ip.startsWith("::1"))     return false;
        if (ip.startsWith("172.")) {
            try {
                int second = Integer.parseInt(ip.split("\\.")[1]);
                if (second >= 16 && second <= 31) return false;
            } catch (Exception ignored) {}
        }
        return true;
    }

    @Override
    public Map<String, Class<?>> getAcceptedParameters() {
        return Map.of(
            "captureSeconds", Integer.class,
            "maxPackets",     Integer.class,
            "bpfFilter",      String.class,
            "includeAll",     Boolean.class
        );
    }

    public static void main(String[] args) throws Exception {
        CollectorPacketSniffer collector = new CollectorPacketSniffer();
        collector.captureSeconds = 5;
        collector.maxPackets     = 50;
        collector.bpfFilter      = "ip";

        System.out.println("=== Packet Sniffer Collector Test ===");
        System.out.println("Cattura in corso per " + collector.captureSeconds + " secondi...");
        CollectorResult result = collector.collect();
        System.out.println(result.getResult().toJSONString());
    }
}