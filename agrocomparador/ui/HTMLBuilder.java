package agrocomparador.ui;

import agrocomparador.business.ProductoService;
import agrocomparador.data.InformeSemanalDAO;
import agrocomparador.data.MinisterioExcelDAO;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class HTMLBuilder {

    // ─── Productos objetivo para tarjetas de tendencia ───────────────────────
    private static final String[] PROD_TARJETA_NOMBRE = {"Tomate","Pimiento","Berenjena","Judía","Calabacín","Pepino"};
    private static final String[] PROD_TARJETA_KEY    = {"tomate","pimiento","berenjena","judia","calabacin","pepino"};

    // ─── Alias de nombres de productos ───────────────────────────────────────
    // Clave: prefijo en minúsculas sin tildes → Valor: nombre canónico
    private static final Map<String, String> ALIAS_PRODUCTOS;
    static {
        ALIAS_PRODUCTOS = new LinkedHashMap<>();
        ALIAS_PRODUCTOS.put("pto",       "Pimiento");
        ALIAS_PRODUCTOS.put("pto.",      "Pimiento");
        // Añadir aquí más alias según vayan apareciendo
    }

    // Devuelve el nombre canónico del producto (resuelve abreviaciones/alias)
    private static String canonico(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) return "";
        String raw   = nombre.trim();
        String lower = raw.toLowerCase()
            .replace("á","a").replace("é","e").replace("í","i")
            .replace("ó","o").replace("ú","u").replace("ñ","n");
        for (Map.Entry<String, String> e : ALIAS_PRODUCTOS.entrySet()) {
            String alias = e.getKey();
            if (lower.equals(alias))                 return e.getValue();
            if (lower.startsWith(alias + " "))       return e.getValue() + raw.substring(alias.length());
        }
        return raw;
    }

    // ─── Punto de entrada principal ──────────────────────────────────────────

    public static String construirRespuestaHTML(List<Map<String, Object>> productos,
            String error, String filtroAplicado, String accion, String filtroFechaDesde, String filtroFechaHasta, String filtroCategoria) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang='es'>\n<head>\n");
        html.append("<meta charset='UTF-8'>\n");
        html.append("<meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        html.append("<title>AgroComparador — Precios Agrícolas</title>\n");
        html.append("<script src='https://cdn.jsdelivr.net/npm/chart.js@4/dist/chart.umd.min.js'></script>\n");
        html.append(construirCSS());
        html.append("</head>\n<body>\n");

        html.append(construirHeader());

        html.append("<div class='container'>\n");

        if (accion != null && !accion.isEmpty())
            html.append(construirBanner(accion));

        html.append(construirTarjetasProductos());
        html.append(construirToolbar(filtroAplicado, filtroFechaDesde, filtroFechaHasta, filtroCategoria));

        if (error != null && !error.isEmpty())
            html.append("<div class='alert alert-error'><span>⚠️</span> ").append(escapeHTML(error)).append("</div>\n");

        if (productos != null && !productos.isEmpty()) {
            html.append("<p class='results-info'>").append(productos.size()).append(" registros encontrados</p>\n");
            html.append(construirGrafica(productos));
            html.append(construirGraficaExcel(productos));
            html.append(construirTabla(productos));
        } else {
            html.append("<div class='empty-state'>");
            html.append("<div class='empty-icon'>📭</div>");
            html.append("<p>No hay datos disponibles</p>");
            html.append("<small>Usa el botón <strong>Cargar datos</strong> para iniciar la descarga</small>");
            html.append("</div>\n");
        }

        html.append("</div>\n");
        try { html.append(construirModalCarga(ProductoService.obtenerFechasDisponibles())); }
        catch (Exception ignored) { html.append(construirModalCarga(new ArrayList<>())); }
        try { html.append(construirModalVaciar(ProductoService.obtenerFechasDisponibles())); }
        catch (Exception ignored) { html.append(construirModalVaciar(new ArrayList<>())); }
        html.append("</body>\n</html>");
        return html.toString();
    }

    public static String construirRespuestaHTML(List<Map<String, Object>> productos,
            String error, String filtroAplicado, String accion, String filtroFechaDesde, String filtroFechaHasta) {
        return construirRespuestaHTML(productos, error, filtroAplicado, accion, filtroFechaDesde, filtroFechaHasta, null);
    }

    public static String construirRespuestaHTML(List<Map<String, Object>> productos,
            String error, String filtroAplicado, String accion) {
        return construirRespuestaHTML(productos, error, filtroAplicado, accion, null, null, null);
    }

    public static String construirRespuestaHTML(List<Map<String, Object>> productos,
            String error, String filtroAplicado) {
        return construirRespuestaHTML(productos, error, filtroAplicado, null, null, null, null);
    }

    // ─── CSS ─────────────────────────────────────────────────────────────────

    private static String construirCSS() {
        return "<style>\n"
            + "*, *::before, *::after { margin:0; padding:0; box-sizing:border-box; }\n"
            + "body { font-family:'Segoe UI',system-ui,-apple-system,sans-serif; background:#F0F4F8; color:#1A2332; font-size:14px; line-height:1.5; }\n"

            // Header
            + ".site-header { background:linear-gradient(135deg,#1B5E20 0%,#2E7D32 60%,#43A047 100%); color:white; padding:20px 0; box-shadow:0 4px 16px rgba(0,0,0,0.18); }\n"
            + ".header-inner { max-width:1100px; margin:0 auto; padding:0 24px; display:flex; align-items:center; justify-content:space-between; flex-wrap:wrap; gap:12px; }\n"
            + ".header-brand { display:flex; align-items:center; gap:14px; }\n"
            + ".header-icon { font-size:38px; }\n"
            + ".header-brand h1 { font-size:22px; font-weight:700; letter-spacing:-0.3px; }\n"
            + ".header-brand p { font-size:12px; opacity:0.82; margin-top:2px; }\n"
            + ".header-sources { display:flex; gap:8px; flex-wrap:wrap; }\n"
            + ".src-badge { padding:5px 14px; border-radius:20px; font-size:12px; font-weight:600; background:rgba(255,255,255,0.15); backdrop-filter:blur(4px); }\n"

            // Container
            + ".container { max-width:1100px; margin:0 auto; padding:24px; }\n"

            // Stats grid
            + ".stats-grid { display:grid; grid-template-columns:repeat(4,1fr); gap:14px; margin-bottom:18px; }\n"
            + ".stat-card { background:white; border-radius:12px; padding:16px 18px; box-shadow:0 1px 3px rgba(0,0,0,0.06),0 4px 12px rgba(0,0,0,0.04); border-top:3px solid; }\n"
            + ".stat-card.c-green { border-top-color:#2E7D32; }\n"
            + ".stat-card.c-blue  { border-top-color:#1565C0; }\n"
            + ".stat-card.c-orange{ border-top-color:#E64A19; }\n"
            + ".stat-card.c-gray  { border-top-color:#78909C; }\n"
            + ".stat-val  { font-size:22px; font-weight:700; color:#1A2332; }\n"
            + ".stat-lbl  { font-size:11px; color:#78909C; margin-top:4px; text-transform:uppercase; letter-spacing:0.5px; }\n"

            // Toolbar (search + buttons)
            + ".toolbar { background:white; border-radius:12px; box-shadow:0 1px 3px rgba(0,0,0,0.06),0 4px 12px rgba(0,0,0,0.04); padding:14px 18px; margin-bottom:18px; display:flex; gap:10px; align-items:center; flex-wrap:wrap; }\n"
            + ".search-wrap { flex:1; min-width:200px; position:relative; }\n"
            + ".search-wrap::before { content:'🔍'; position:absolute; left:12px; top:50%; transform:translateY(-50%); font-size:14px; pointer-events:none; }\n"
            + ".search-wrap input { width:100%; padding:9px 14px 9px 38px; border:1.5px solid #E0E7EF; border-radius:8px; font-size:14px; color:#1A2332; outline:none; transition:border-color 0.2s; background:#FAFBFC; }\n"
            + ".search-wrap input:focus { border-color:#2E7D32; background:white; }\n"
            + ".sugg-list { display:none; position:absolute; top:calc(100% + 4px); left:0; right:0; background:white; border:1.5px solid #E0E7EF; border-radius:10px; box-shadow:0 6px 20px rgba(0,0,0,.10); z-index:200; overflow:hidden; }\n"
            + ".sugg-item { padding:9px 14px 9px 38px; cursor:pointer; font-size:13px; color:#1A2332; border-bottom:1px solid #F0F4F8; }\n"
            + ".sugg-item:last-child { border-bottom:none; }\n"
            + ".sugg-item:hover { background:#F0F4F8; }\n"
            // Chip multi-select input
            + ".chips-input-wrap { flex:1; min-width:200px; position:relative; background:#FAFBFC; border:1.5px solid #E0E7EF; border-radius:8px; cursor:text; }\n"
            + ".chips-input-wrap:focus-within { border-color:#2E7D32; background:white; }\n"
            + ".chips-area { display:flex; flex-wrap:wrap; align-items:center; gap:4px; padding:4px 8px 4px 34px; min-height:38px; position:relative; }\n"
            + ".chips-area::before { content:'🔍'; position:absolute; left:12px; top:50%; transform:translateY(-50%); font-size:14px; pointer-events:none; }\n"
            + ".chip-tag { display:inline-flex; align-items:center; gap:3px; padding:2px 6px 2px 8px; background:#C8E6C9; border-radius:12px; font-size:12px; color:#1B5E20; font-weight:600; white-space:nowrap; }\n"
            + ".chip-x { cursor:pointer; margin-left:2px; color:#4E7D54; font-size:14px; line-height:1; display:inline-block; }\n"
            + ".chip-x:hover { color:#C62828; }\n"
            + "#chip-search { border:none; outline:none; background:transparent; font-size:14px; color:#1A2332; min-width:60px; flex:1; padding:1px 4px; }\n"
            // Producto cards (tendencias)
            + ".prod-cards { display:grid; grid-template-columns:repeat(6,1fr); gap:12px; margin-bottom:18px; }\n"
            + ".prod-card { border-radius:12px; padding:14px 10px; box-shadow:0 1px 3px rgba(0,0,0,.06); display:flex; flex-direction:column; align-items:center; text-align:center; gap:5px; transition:transform 0.15s; border-left:4px solid transparent; }\n"
            + ".prod-card:hover { transform:translateY(-2px); }\n"
            + ".card-up { background:#E8F5E9; border-left-color:#4CAF50; }\n"
            + ".card-down { background:#FFEBEE; border-left-color:#EF5350; }\n"
            + ".card-neutral { background:#F5F5F5; border-left-color:#9E9E9E; }\n"
            // Price coloring in table
            + ".precio-bajo { color:#2E7D32 !important; }\n"
            + ".precio-alto { color:#C62828 !important; }\n"
            + ".sep { width:1px; height:30px; background:#E0E7EF; margin:0 4px; flex-shrink:0; }\n"
            + ".btn { padding:9px 16px; border:none; border-radius:8px; font-size:13px; font-weight:600; cursor:pointer; transition:all 0.15s; text-decoration:none; display:inline-flex; align-items:center; gap:5px; white-space:nowrap; }\n"
            + ".btn-primary { background:#2E7D32; color:white; } .btn-primary:hover { background:#1B5E20; }\n"
            + ".btn-ghost   { background:#EEF2F7; color:#5A6779; } .btn-ghost:hover   { background:#DDE4EE; color:#1A2332; }\n"
            + ".btn-blue    { background:#1565C0; color:white; } .btn-blue:hover    { background:#0D47A1; }\n"
            + ".btn-danger  { background:#C62828; color:white; } .btn-danger:hover  { background:#B71C1C; }\n"

            // Banners
            + ".banner { display:flex; align-items:center; gap:10px; padding:12px 16px; border-radius:10px; margin-bottom:16px; font-weight:500; font-size:14px; border:1px solid; }\n"
            + ".banner-success { background:#E8F5E9; border-color:#A5D6A7; color:#1B5E20; }\n"
            + ".banner-info    { background:#E3F2FD; border-color:#90CAF9; color:#0D47A1; }\n"

            // Alert
            + ".alert { display:flex; align-items:center; gap:10px; padding:12px 16px; border-radius:10px; margin-bottom:16px; font-size:14px; border:1px solid; }\n"
            + ".alert-error { background:#FFEBEE; border-color:#FFCDD2; color:#B71C1C; }\n"

            // Results info
            + ".results-info { font-size:13px; color:#78909C; margin-bottom:12px; padding:0 2px; }\n"

            // Chart card
            + ".card { background:white; border-radius:12px; box-shadow:0 1px 3px rgba(0,0,0,0.06),0 4px 12px rgba(0,0,0,0.04); margin-bottom:18px; overflow:hidden; }\n"
            + ".card-head { display:flex; align-items:center; justify-content:space-between; padding:14px 20px; border-bottom:1px solid #EEF2F7; }\n"
            + ".card-head h2 { font-size:14px; font-weight:600; color:#1A2332; }\n"
            + ".card-note { font-size:12px; color:#78909C; }\n"
            + ".chart-wrap { padding:16px 20px 20px; height:300px; }\n"
            + ".chart-legend { display:flex; gap:16px; padding:0 20px 14px; flex-wrap:wrap; }\n"
            + ".legend-item { display:flex; align-items:center; gap:6px; font-size:12px; color:#5A6779; }\n"
            + ".legend-dot { width:10px; height:10px; border-radius:50%; flex-shrink:0; }\n"

            // Table
            + "table { width:100%; border-collapse:collapse; }\n"
            + "thead { background:linear-gradient(135deg,#1B5E20,#2E7D32); }\n"
            + "th { padding:12px 16px; text-align:left; font-size:11px; font-weight:700; text-transform:uppercase; letter-spacing:0.6px; color:rgba(255,255,255,0.9); white-space:nowrap; }\n"
            + "td { padding:11px 16px; border-bottom:1px solid #F0F4F8; font-size:13px; }\n"
            + "tbody tr:last-child td { border-bottom:none; }\n"
            + "tbody tr { background:white; }\n"
            + "tbody tr:hover { background:#F0F4F8 !important; }\n"
            + "tbody tr.row-bd           { background:#F1FBF2; } tbody tr.row-bd:hover           { background:#DCEEDE !important; }\n"
            + "tbody tr.row-agroprecios  { background:#EEF5FF; } tbody tr.row-agroprecios:hover  { background:#D8E8FF !important; }\n"
            + "tbody tr.row-agropizarra  { background:#FFF3EE; } tbody tr.row-agropizarra:hover  { background:#FFE0CC !important; }\n"
            + ".precio { font-weight:700; color:#1B5E20; font-size:14px; }\n"
            + ".orn { display:inline-flex; align-items:center; gap:4px; padding:3px 10px; border-radius:20px; font-size:11px; font-weight:600; }\n"
            + ".orn-bd          { background:#C8E6C9; color:#1B5E20; }\n"
            + ".orn-agroprecios { background:#BBDEFB; color:#1565C0; }\n"
            + ".orn-agropizarra { background:#FFCCBC; color:#BF360C; }\n"
            + ".var-empty { color:#C5CDD8; font-style:italic; }\n"
            + ".prod-icon { font-size:15px; vertical-align:middle; margin-right:4px; }\n"
            + ".date-select { padding:8px 12px; border:1.5px solid #E0E7EF; border-radius:8px; font-size:13px; color:#1A2332; background:#FAFBFC; outline:none; cursor:pointer; transition:border-color 0.2s; }\n"
            + ".date-select:focus { border-color:#2E7D32; background:white; }\n"
            + ".period-btns { display:flex; gap:6px; padding:8px 20px 12px; flex-wrap:wrap; border-bottom:1px solid #EEF2F7; align-items:center; }\n"
            + ".period-btn { padding:4px 14px; border:1.5px solid #D0D8E4; border-radius:20px; font-size:12px; font-weight:600; cursor:pointer; background:white; color:#5A6779; transition:all 0.15s; }\n"
            + ".period-btn:hover { border-color:#2E7D32; color:#2E7D32; }\n"
            + ".period-btn.active { background:#2E7D32; border-color:#2E7D32; color:white; }\n"
            + ".period-btn.evo-btn { border-color:#7B1FA2; color:#7B1FA2; }\n"
            + ".period-btn.evo-btn:hover { background:#7B1FA2; color:white; }\n"
            + ".period-btn.evo-btn.active { background:#7B1FA2; border-color:#7B1FA2; color:white; }\n"
            + ".period-sep { width:1px; height:20px; background:#D0D8E4; margin:0 4px; }\n"
            + ".group-header { cursor:pointer; }\n"
            + ".group-header td { background:#EEF2F7 !important; font-weight:700; font-size:13px; border-top:2px solid #D0D8E4; }\n"
            + ".group-header:hover td { background:#E2E8F2 !important; }\n"
            + ".subgroup-header { cursor:pointer; display:none; }\n"
            + ".subgroup-header td { background:#F7F9FC !important; font-weight:600; font-size:12px; border-top:1px solid #E8EEF5; }\n"
            + ".subgroup-header td:first-child { padding-left:32px !important; }\n"
            + ".subgroup-header:hover td { background:#EDF1F8 !important; }\n"
            + ".subgroup-header.open .sub-toggle { transform:rotate(90deg); }\n"
            + ".sub-toggle { display:inline-block; transition:transform 0.2s; margin-right:5px; color:#A0AABB; font-size:11px; font-style:normal; }\n"
            + ".toggle-icon { display:inline-block; transition:transform 0.2s; margin-right:6px; color:#78909C; font-style:normal; }\n"
            + ".group-header.open .toggle-icon { transform:rotate(90deg); }\n"
            + ".group-child { display:none; }\n"
            + ".group-count { color:#78909C; font-size:12px; font-weight:400; }\n"
            + ".child-indent { color:#C5CDD8; font-size:14px; padding-left:42px !important; }\n"
            + ".fecha-cell { font-size:11px; color:#78909C; white-space:nowrap; }\n"
            + ".tbl-actions { display:flex; gap:6px; }\n"
            + ".btn-xs { padding:3px 10px; font-size:11px; border:1.5px solid #D0D8E4; border-radius:6px; cursor:pointer; background:white; color:#5A6779; font-weight:600; transition:all 0.15s; }\n"
            + ".btn-xs:hover { border-color:#2E7D32; color:#2E7D32; }\n"

            // Modal de carga
            + ".modal-overlay { display:none; position:fixed; inset:0; background:rgba(0,0,0,0.45); z-index:1000; align-items:center; justify-content:center; }\n"
            + ".modal-box { background:white; border-radius:14px; padding:28px; max-width:440px; width:92%; box-shadow:0 20px 60px rgba(0,0,0,0.25); }\n"
            + ".modal-title { font-size:16px; font-weight:700; color:#1A2332; margin-bottom:6px; }\n"
            + ".modal-sub { font-size:13px; color:#78909C; margin-bottom:18px; }\n"
            + ".dia-option { display:flex; align-items:center; justify-content:space-between; padding:10px 14px; border-radius:8px; cursor:pointer; transition:background 0.15s; border:1.5px solid #E0E7EF; }\n"
            + ".dia-option:hover:not(.dia-cargado):not(.dia-en-proceso) { border-color:#1565C0; background:#EEF5FF; }\n"
            + ".dia-option.dia-cargado { background:#F8F8F8; color:#BBBBBB; cursor:default; }\n"
            + ".dia-label { display:flex; align-items:center; gap:8px; font-size:13px; }\n"
            + ".dia-badge { font-size:11px; padding:2px 9px; border-radius:10px; font-weight:600; white-space:nowrap; }\n"
            + ".dia-badge-ok   { background:#C8E6C9; color:#2E7D32; }\n"
            + ".dia-badge-new  { background:#BBDEFB; color:#1565C0; }\n"
            + ".dia-badge-proc { background:#FFF3E0; color:#E65100; }\n"
            + ".dia-en-proceso { background:#FFFDE7; color:#AAAAAA; cursor:default; border-color:#FFE082; }\n"
            + ".dia-sin-datos { background:#F8F8F8; color:#CCCCCC; cursor:default; border-color:#EEEEEE; }\n"
            + ".dia-vaciar { border-color:#EF9A9A; } .dia-vaciar:hover:not(.dia-sin-datos) { background:#FFEBEE; border-color:#C62828; }\n"
            + ".dia-badge-danger { background:#FFCDD2; color:#C62828; }\n"

            // Empty state
            + ".empty-state { text-align:center; padding:60px 20px; color:#78909C; background:white; border-radius:12px; box-shadow:0 1px 3px rgba(0,0,0,0.06); }\n"
            + ".empty-icon { font-size:52px; margin-bottom:14px; }\n"
            + ".empty-state p { font-size:16px; font-weight:600; color:#1A2332; margin-bottom:8px; }\n"
            + ".empty-state small { font-size:13px; }\n"

            // Responsive
            + "@media(max-width:900px) { .prod-cards { grid-template-columns:repeat(3,1fr); } }\n"
            + "@media(max-width:720px) { .header-sources { display:none; } .sep { display:none; } }\n"
            + "@media(max-width:500px) { .prod-cards { grid-template-columns:repeat(2,1fr); } }\n"
            + "</style>\n";
    }

    // ─── Header ───────────────────────────────────────────────────────────────

    private static String construirHeader() {
        return "<header class='site-header'>\n"
            + "  <div class='header-inner'>\n"
            + "    <div class='header-brand'>\n"
            + "      <span class='header-icon'>🌾</span>\n"
            + "      <div>\n"
            + "        <h1>AgroComparador</h1>\n"
            + "        <p>Comparador de precios agrícolas en tiempo real</p>\n"
            + "      </div>\n"
            + "    </div>\n"
            + "    <div class='header-sources'>\n"
            + "      <span class='src-badge'>🌐 AgroPrecios.com</span>\n"
            + "      <span class='src-badge'>📊 AgroPizarra.com</span>\n"
            + "    </div>\n"
            + "  </div>\n"
            + "</header>\n";
    }

    // ─── Banner de acción ─────────────────────────────────────────────────────

    private static String construirBanner(String accion) {
        if ("vaciado".equals(accion))
            return "<div class='banner banner-success'>✅ Datos vaciados correctamente.</div>\n";
        if ("cargando".equals(accion))
            return "<div class='banner banner-info'>⏳ Carga iniciada en segundo plano — los resultados aparecerán en unos minutos.</div>\n";
        if ("error_clave".equals(accion))
            return "<div class='banner' style='background:#FFEBEE;border-color:#FFCDD2;color:#B71C1C;'>❌ Contraseña incorrecta. No se han eliminado datos.</div>\n";
        return "";
    }

    // ─── Resumen de la semana ────────────────────────────────────────────────

    private static String construirResumenSemana() {
        Map<String, Map<String, Object>> res;
        try { res = InformeSemanalDAO.obtenerResumenSemana(); }
        catch (Exception e) { return ""; }
        if (res == null || res.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        sb.append("<div style='display:grid;grid-template-columns:repeat(3,1fr);gap:14px;margin-bottom:18px'>\n");

        Map<String, Object> caro   = res.get("masCaros");
        Map<String, Object> subida = res.get("mayorSubida");
        Map<String, Object> bajada = res.get("mayorBajada");

        if (caro != null)   sb.append(tarjetaResumen("💰", "Más caro esta semana",
            limpiarNombreSemanal(caro.get("producto")),
            String.format("%.2f €/100 kg", caro.get("precio")), "", "#1565C0", "#E3F2FD", "#0D47A1"));
        if (subida != null) { double v = ((Number)subida.get("variacion")).doubleValue();
            sb.append(tarjetaResumen("📈", "Mayor subida semanal",
            limpiarNombreSemanal(subida.get("producto")),
            String.format("%+.2f%%", v), v > 0 ? "▲" : "", "#2E7D32", "#E8F5E9", "#1B5E20")); }
        if (bajada != null) { double v = ((Number)bajada.get("variacion")).doubleValue();
            sb.append(tarjetaResumen("📉", "Mayor bajada semanal",
            limpiarNombreSemanal(bajada.get("producto")),
            String.format("%+.2f%%", v), v < 0 ? "▼" : "", "#C62828", "#FFEBEE", "#B71C1C")); }

        sb.append("</div>\n");
        return sb.toString();
    }

    private static String tarjetaResumen(String icono, String titulo, String producto, String valor, String flecha, String colorBorde, String colorFondo, String colorTexto) {
        return "<div style='background:" + colorFondo + ";border-radius:12px;padding:16px 18px;border-left:4px solid " + colorBorde + ";box-shadow:0 1px 3px rgba(0,0,0,.06)'>\n"
            + "  <div style='font-size:11px;font-weight:700;text-transform:uppercase;letter-spacing:.6px;color:" + colorBorde + ";margin-bottom:6px'>" + icono + " " + titulo + "</div>\n"
            + "  <div style='font-size:13px;font-weight:600;color:#1A2332;margin-bottom:4px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis'>" + escapeHTML(producto) + "</div>\n"
            + "  <div style='font-size:20px;font-weight:700;color:" + colorTexto + "'>" + flecha + " " + escapeHTML(valor) + "</div>\n"
            + "</div>\n";
    }

    private static String limpiarNombreSemanal(Object obj) {
        if (obj == null) return "";
        return obj.toString().replaceAll("\\s*\\([^)]+\\)\\*?$", "").trim();
    }

    // ─── Tarjetas de tendencia por producto ──────────────────────────────────

    private static String construirTarjetasProductos() {
        // Datos reales del scraper web (tabla precios, origen AGROPRECIOS/AGROPIZARRA)
        List<Map<String, Object>> tendencias = new ArrayList<>();
        try { tendencias = ProductoService.obtenerTendenciasScraperProductos(); } catch (Exception ignored) {}

        // Normalizar: prodNorm → datos de tendencia
        Map<String, Map<String, Object>> lookup = new LinkedHashMap<>();
        for (Map<String, Object> t : tendencias) {
            String prod = normalizarMatch(t.getOrDefault("nombre", "").toString());
            lookup.put(prod, t);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<div class='prod-cards'>\n");

        for (int i = 0; i < PROD_TARJETA_NOMBRE.length; i++) {
            String name  = PROD_TARJETA_NOMBRE[i];
            String key   = PROD_TARJETA_KEY[i];
            String icono = iconoProducto(key);
            String imgSrc = "/img/" + key + ".jpg";

            // Buscar coincidencia: igual, contiene, o contenido por
            Map<String, Object> data = null;
            for (Map.Entry<String, Map<String, Object>> e : lookup.entrySet()) {
                String k = e.getKey();
                if (k.equals(key) || k.startsWith(key) || key.startsWith(k)) {
                    data = e.getValue(); break;
                }
            }

            double actual = 0, variacion = 0;
            String fecha = "";
            if (data != null) {
                actual    = ((Number) data.getOrDefault("actual",    0.0)).doubleValue();
                variacion = ((Number) data.getOrDefault("variacion", 0.0)).doubleValue();
                fecha     = data.getOrDefault("fecha", "").toString();
            }

            boolean rising  = variacion >  0.05;
            boolean falling = variacion < -0.05;
            boolean hasData = actual > 0;

            String cardCls    = hasData ? (rising ? "card-up" : (falling ? "card-down" : "card-neutral")) : "card-neutral";
            String trendColor = rising  ? "#2E7D32" : (falling ? "#C62828" : "#78909C");
            String trendArrow = rising  ? "▲"       : (falling ? "▼"       : "—");
            String trendTxt   = (variacion != 0) ? String.format(Locale.US, "%+.1f%%", variacion) : "estable";

            sb.append("  <div class='prod-card ").append(cardCls).append("'>\n");
            // Miniatura circular con fallback al emoji
            sb.append("    <div style='width:56px;height:56px;border-radius:50%;overflow:hidden;border:2px solid rgba(0,0,0,.08);flex-shrink:0'>\n");
            sb.append("      <img src='").append(imgSrc).append("' alt='").append(escapeHTML(name))
              .append("' style='width:100%;height:100%;object-fit:cover' onerror=\"this.parentElement.innerHTML='<span style=\\'font-size:32px;line-height:56px;display:block;text-align:center\\'>");
            sb.append(icono).append("</span>'\">\n");
            sb.append("    </div>\n");
            sb.append("    <div style='font-size:12px;font-weight:700;color:#1A2332;margin-top:2px'>").append(escapeHTML(name)).append("</div>\n");
            if (hasData) {
                sb.append("    <div style='font-size:13px;font-weight:600;color:#1A2332'>€")
                  .append(String.format(Locale.US, "%.2f", actual)).append("/kg</div>\n");
                sb.append("    <div style='font-size:14px;font-weight:700;color:").append(trendColor).append(";'>")
                  .append(trendArrow).append(" ").append(escapeHTML(trendTxt)).append("</div>\n");
                if (!fecha.isEmpty())
                    sb.append("    <div style='font-size:10px;color:#9E9E9E'>").append(escapeHTML(fecha)).append("</div>\n");
            } else {
                sb.append("    <div style='font-size:11px;color:#9E9E9E;margin-top:6px'>Sin datos</div>\n");
            }
            sb.append("  </div>\n");
        }

        sb.append("</div>\n");
        return sb.toString();
    }

    // ─── Stats grid ──────────────────────────────────────────────────────────

    private static String construirStatsGrid() {
        String total = "—", unicos = "—", promedio = "—", fecha = "—";
        try {
            Map<String, String> s = ProductoService.obtenerEstadisticasScraper();
            if (s != null && !s.isEmpty()) {
                total    = s.getOrDefault("total_registros",   "0");
                unicos   = s.getOrDefault("productos_unicos",  "0");
                promedio = "€" + s.getOrDefault("precio_promedio", "0");
                String u = s.getOrDefault("ultima_actualizacion", "");
                fecha    = u.isEmpty() ? "—" : u.substring(0, Math.min(16, u.length()));
            }
        } catch (Exception ignored) {}

        return "<div class='stats-grid'>\n"
            + statCard("c-green",  total,    "Registros totales")
            + statCard("c-blue",   unicos,   "Productos únicos")
            + statCard("c-orange", promedio, "Precio promedio")
            + statCard("c-gray",   fecha,    "Última actualización")
            + "</div>\n";
    }

    private static String statCard(String cls, String val, String lbl) {
        return "  <div class='stat-card " + cls + "'>"
            + "<div class='stat-val'>" + escapeHTML(val) + "</div>"
            + "<div class='stat-lbl'>" + lbl + "</div></div>\n";
    }

    // ─── Toolbar (búsqueda + acciones) ────────────────────────────────────────

    private static String construirToolbar(String filtro, String filtroFechaDesde, String filtroFechaHasta, String categoria) {
        List<String> fechas = new ArrayList<>();
        try { fechas = ProductoService.obtenerFechasDisponibles(); } catch (Exception ignored) {}

        // fechas ordenadas DESC: primero el más reciente, último el más antiguo
        String maxFecha = fechas.isEmpty() ? "" : fechas.get(0);
        String minFecha = fechas.isEmpty() ? "" : fechas.get(fechas.size() - 1);

        StringBuilder tb = new StringBuilder();
        tb.append("<div class='toolbar'>\n");
        tb.append("  <form method='GET' action='/' style='display:flex;gap:8px;flex:1;min-width:0;align-items:center;flex-wrap:wrap;'>\n");

        // Autocomplete multi-selección con chips
        List<String> sugerencias = new ArrayList<>();
        try { sugerencias = ProductoService.obtenerNombresProductos(); } catch (Exception ignored) {}

        // Array JS con los productos actualmente seleccionados (del filtro URL)
        String filtroInitJS = "[]";
        if (filtro != null && !filtro.trim().isEmpty()) {
            String[] parts = filtro.split(",");
            StringBuilder initArr = new StringBuilder("[");
            for (int idx = 0; idx < parts.length; idx++) {
                if (idx > 0) initArr.append(",");
                initArr.append("'").append(escapeJS(parts[idx].trim())).append("'");
            }
            initArr.append("]");
            filtroInitJS = initArr.toString();
        }

        tb.append("    <div class='chips-input-wrap'>\n");
        tb.append("      <div class='chips-area' id='chips-area'>\n");
        tb.append("        <input type='text' id='chip-search' autocomplete='off'");
        tb.append(" onfocus='showSugg()' oninput='showSugg()' onblur='hideSugg()'>\n");
        tb.append("      </div>\n");
        tb.append("      <input type='hidden' name='producto' id='producto-hidden'>\n");
        if (!sugerencias.isEmpty()) {
            tb.append("      <div class='sugg-list' id='sugg-list'>\n");
            for (String nombre : sugerencias) {
                String icono = iconoProducto(nombre.toLowerCase()
                    .replace("á","a").replace("é","e").replace("í","i")
                    .replace("ó","o").replace("ú","u").replace("ñ","n"));
                tb.append("        <div class='sugg-item' data-nombre='").append(escapeHTML(nombre))
                  .append("' onmousedown=\"setSugg('").append(escapeJS(nombre))
                  .append("')\">").append(icono).append(" ").append(escapeHTML(nombre)).append("</div>\n");
            }
            tb.append("      </div>\n");
        }
        tb.append("    </div>\n");
        tb.append("<script>\n");
        tb.append("(function(){\n");
        tb.append("  var sel=").append(filtroInitJS).append(";\n");
        tb.append("  function escH(s){return s.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');}\n");
        tb.append("  function renderChips(){\n");
        tb.append("    var area=document.getElementById('chips-area');\n");
        tb.append("    area.querySelectorAll('.chip-tag').forEach(function(c){c.remove();});\n");
        tb.append("    var inp=document.getElementById('chip-search');\n");
        tb.append("    sel.forEach(function(name){\n");
        tb.append("      var span=document.createElement('span');\n");
        tb.append("      span.className='chip-tag';\n");
        tb.append("      span.dataset.rm=encodeURIComponent(name);\n");
        tb.append("      span.innerHTML=escH(name)+'<span class=\"chip-x\">&#215;</span>';\n");
        tb.append("      area.insertBefore(span,inp);\n");
        tb.append("    });\n");
        tb.append("    document.getElementById('producto-hidden').value=sel.join(',');\n");
        tb.append("    inp.placeholder=sel.length===0?'Buscar producto...':'';\n");
        tb.append("  }\n");
        tb.append("  document.addEventListener('click',function(e){\n");
        tb.append("    if(e.target.classList.contains('chip-x')){\n");
        tb.append("      var name=decodeURIComponent(e.target.parentElement.dataset.rm);\n");
        tb.append("      sel=sel.filter(function(x){return x!==name;});\n");
        tb.append("      renderChips();\n");
        tb.append("    }\n");
        tb.append("  });\n");
        tb.append("  window.setSugg=function(n){\n");
        tb.append("    if(sel.indexOf(n)===-1){sel.push(n);renderChips();}\n");
        tb.append("    document.getElementById('chip-search').value='';\n");
        tb.append("    showSugg();\n");
        tb.append("  };\n");
        tb.append("  window.showSugg=function(){\n");
        tb.append("    var v=document.getElementById('chip-search').value.toLowerCase();\n");
        tb.append("    var items=document.querySelectorAll('.sugg-item');var vis=0;\n");
        tb.append("    items.forEach(function(it){\n");
        tb.append("      var n=it.dataset.nombre;\n");
        tb.append("      var m=(v===''||n.toLowerCase().includes(v))&&sel.indexOf(n)===-1;\n");
        tb.append("      it.style.display=m?'':'none';if(m)vis++;\n");
        tb.append("    });\n");
        tb.append("    var l=document.getElementById('sugg-list');if(l)l.style.display=vis>0?'block':'none';\n");
        tb.append("  };\n");
        tb.append("  window.hideSugg=function(){\n");
        tb.append("    setTimeout(function(){var l=document.getElementById('sugg-list');if(l)l.style.display='none';},150);\n");
        tb.append("  };\n");
        tb.append("  document.getElementById('chips-area').addEventListener('click',function(){\n");
        tb.append("    document.getElementById('chip-search').focus();\n");
        tb.append("  });\n");
        tb.append("  renderChips();\n");
        tb.append("})();\n");
        tb.append("</script>\n");

        // Selector de rango de fechas
        if (!fechas.isEmpty()) {
            tb.append("    <div style='display:flex;align-items:center;gap:5px;'>\n");
            tb.append("      <input type='date' name='fechaDesde' class='date-select'");
            if (!minFecha.isEmpty()) tb.append(" min='").append(escapeHTML(minFecha)).append("'");
            if (!maxFecha.isEmpty()) tb.append(" max='").append(escapeHTML(maxFecha)).append("'");
            if (filtroFechaDesde != null && !filtroFechaDesde.isEmpty())
                tb.append(" value='").append(escapeHTML(filtroFechaDesde)).append("'");
            tb.append(" title='Desde'>\n");
            tb.append("      <span style='color:#78909C;font-size:13px;'>→</span>\n");
            tb.append("      <input type='date' name='fechaHasta' class='date-select'");
            if (!minFecha.isEmpty()) tb.append(" min='").append(escapeHTML(minFecha)).append("'");
            if (!maxFecha.isEmpty()) tb.append(" max='").append(escapeHTML(maxFecha)).append("'");
            if (filtroFechaHasta != null && !filtroFechaHasta.isEmpty())
                tb.append(" value='").append(escapeHTML(filtroFechaHasta)).append("'");
            tb.append(" title='Hasta'>\n");
            tb.append("    </div>\n");
        }

        tb.append("    <button type='submit' class='btn btn-primary'>Buscar</button>\n");
        boolean hayFiltro = (filtro != null && !filtro.isEmpty())
                         || (filtroFechaDesde != null && !filtroFechaDesde.isEmpty())
                         || (filtroFechaHasta  != null && !filtroFechaHasta.isEmpty());
        if (hayFiltro)
            tb.append("    <a href='/' class='btn btn-ghost'>✕ Limpiar</a>\n");
        tb.append("  </form>\n");
        tb.append("  <div class='sep'></div>\n");
        tb.append("  <a href='#' class='btn btn-blue' onclick=\"document.getElementById('modal-carga').style.display='flex';return false;\">⬇️ Cargar datos</a>\n");
        tb.append("  <a href='#' class='btn btn-danger' onclick=\"document.getElementById('modal-vaciar').style.display='flex';return false;\">🗑️ Vaciar</a>\n");
        tb.append("</div>\n");
        return tb.toString();
    }

    private static String formatearFechaCorta(String fecha) {
        if (fecha == null || fecha.length() < 10) return fecha != null ? fecha : "";
        try {
            String[] d = fecha.substring(0, 10).split("-");
            if (d.length == 3) return d[2] + "/" + d[1] + "/" + d[0];
        } catch (Exception ignored) {}
        return fecha;
    }

    // ─── Gráfica Precios Ministerio (Excel) ───────────────────────────────────

    public static String clasificarCategoria(String nombre) {
        String n = nombre.toLowerCase()
            .replace("á","a").replace("é","e").replace("í","i")
            .replace("ó","o").replace("ú","u").replace("ñ","n");
        if (n.contains("naranja") || n.contains("limon") || n.contains("manzana") ||
            n.contains("pera") && !n.contains("perejil") || n.contains("uva") || n.contains("fresa") ||
            n.contains("melocoton") || n.contains("sandia") || n.contains("melon") && !n.contains("melocoton") ||
            n.contains("cereza") || n.contains("kiwi") || n.contains("platano") ||
            n.contains("mango") || n.contains("aguacate") || n.contains("mandarina") ||
            n.contains("clementina") || n.contains("nispero") || n.contains("albaricoque") ||
            n.contains("ciruela") || n.contains("higo") || n.contains("granada") ||
            n.contains("frambuesa") || n.contains("arandano") || n.contains("mora"))
            return "frutas";
        if (n.contains("trigo") || n.contains("cebada") || n.contains("maiz") ||
            n.contains("centeno") || n.contains("avena") || n.contains("arroz") ||
            n.contains("colza") || n.contains("girasol") || n.contains("soja") ||
            n.contains("alfalfa") || n.contains("garbanzo") || n.contains("lenteja") ||
            n.contains("guisante") || n.contains("haba seca") || n.contains("torta") ||
            n.contains("aceite") || n.contains("orujo") || n.contains("oliva") ||
            n.contains("vino") || n.contains("pipa"))
            return "cereales";
        return "hortalizas";
    }

    private static String normalizarMatch(String s) {
        return s.toLowerCase()
            .replace("á","a").replace("é","e").replace("í","i")
            .replace("ó","o").replace("ú","u").replace("ñ","n")
            .replaceAll("\\s*\\([^)]+\\)\\*?$", "") // strip "(€/100 kg)*" etc.
            .trim();
    }

    // Meses completos en el orden de MinisterioExcelDAO
    private static final String[] MESES_KEY  = {"Enero","Febrero","Marzo","Abril","Mayo","Junio",
                                                  "Julio","Agosto","Septiem.","Octubre","Noviem.","Diciem."};
    private static final String[] MESES_FULL = {"Enero","Febrero","Marzo","Abril","Mayo","Junio",
                                                  "Julio","Agosto","Septiembre","Octubre","Noviembre","Diciembre"};

    private static String construirGraficaExcel(List<Map<String, Object>> productosScraper) {
        List<Map<String, Object>> datos;
        try {
            datos = InformeSemanalDAO.obtenerComparativaProductos();
        } catch (Exception e) {
            return "";
        }
        if (datos == null || datos.isEmpty()) return "";

        // Nombres normalizados del scraper (únicos)
        Set<String> nombresScraper = new LinkedHashSet<>();
        if (productosScraper != null) {
            for (Map<String, Object> p : productosScraper) {
                String n = normalizarMatch(p.getOrDefault("nombre", "").toString());
                if (!n.isEmpty()) nombresScraper.add(n);
            }
        }

        // ── Gráfica de barras: semanal promedio ──
        if (!nombresScraper.isEmpty()) {
            datos = datos.stream()
                .filter(d -> {
                    String prod = normalizarMatch(d.getOrDefault("producto", "").toString());
                    return nombresScraper.stream().anyMatch(n -> prod.contains(n) || n.contains(prod));
                })
                .collect(Collectors.toList());
        }
        if (datos.isEmpty()) return "";

        String[] palette   = {"#2d6a4f","#40916c","#52b788","#74c69d","#95d5b2","#b7e4c7","#d8f3dc"};
        String[] linePal   = {"#2d6a4f","#e63946","#457b9d","#f4a261","#8338ec","#06d6a0","#fb8500"};

        StringBuilder barLabels = new StringBuilder("[");
        StringBuilder barValues = new StringBuilder("[");
        StringBuilder barColors = new StringBuilder("[");
        for (int i = 0; i < datos.size(); i++) {
            Map<String, Object> d = datos.get(i);
            String pCorto = escapeJS(d.getOrDefault("producto","").toString()
                .replaceAll("\\s*\\([^)]+\\)\\*?$","").trim());
            double kg = ((Number)d.getOrDefault("promedio",0.0)).doubleValue() / 100.0;
            if (i > 0) { barLabels.append(","); barValues.append(","); barColors.append(","); }
            barLabels.append("'").append(pCorto).append("'");
            barValues.append(String.format(Locale.US,"%.3f",kg));
            barColors.append("'").append(palette[i % palette.length]).append("'");
        }
        barLabels.append("]"); barValues.append("]"); barColors.append("]");

        // ── Gráfica de evolución mensual: MinisterioExcelDAO ──
        // Estructura JS: evoData[anio][productoLabel] = [p_enero, p_feb, ..., p_dic]
        Map<String, Map<Integer, double[]>> evoDataMap = new LinkedHashMap<>(); // label -> anio -> precios[12]
        TreeSet<Integer> aniosSet = new TreeSet<>();
        List<String> evoLabels = new ArrayList<>(); // display labels de productos en evo

        try {
            List<String> ministProductos = MinisterioExcelDAO.obtenerProductos();
            for (String mp : ministProductos) {
                String mpNorm = normalizarMatch(mp);
                boolean match = nombresScraper.stream().anyMatch(n -> mpNorm.contains(n) || n.contains(mpNorm));
                if (!match) continue;
                String label = mp.replaceAll("\\s*\\([^)]+\\)\\*?$","").trim()
                                 .replace("PIMIENTO","Pimiento").replace("TOMATE","Tomate")
                                 .replace("BERENJENA","Berenjena").replace("CALABACIN","Calabacín")
                                 .replace("PEPINO","Pepino").replace("JUDIA","Judía");
                // Usar el label del primer match (pode haber varios para el mismo normalizado)
                if (evoDataMap.containsKey(label)) continue;
                evoLabels.add(label);
                Map<Integer, double[]> byAnio = new TreeMap<>();
                for (Map<String,Object> row : MinisterioExcelDAO.obtenerHistoricoProducto(mp)) {
                    int a = ((Number)row.get("anio")).intValue();
                    String mes = row.get("mes").toString();
                    double precio = ((Number)row.get("precio")).doubleValue();
                    aniosSet.add(a);
                    byAnio.computeIfAbsent(a, k -> new double[12]);
                    for (int mi = 0; mi < MESES_KEY.length; mi++) {
                        if (MESES_KEY[mi].equals(mes)) { byAnio.get(a)[mi] = precio; break; }
                    }
                }
                evoDataMap.put(label, byAnio);
            }
        } catch (Exception ignored) {}

        List<Integer> anios = new ArrayList<>(aniosSet);
        int anioDefault = anios.contains(2025) ? 2025 : (anios.isEmpty() ? 2025 : anios.get(anios.size() - 1));
        boolean hayEvo = !evoDataMap.isEmpty();

        // Serializar meses
        StringBuilder mesesJs = new StringBuilder("[");
        for (int i = 0; i < MESES_FULL.length; i++) {
            if (i > 0) mesesJs.append(",");
            mesesJs.append("'").append(MESES_FULL[i]).append("'");
        }
        mesesJs.append("]");

        // Serializar evoData como objeto JS: {2024:{label:[...], ...}, 2025:{...}}
        StringBuilder evoJs = new StringBuilder("{");
        for (int ai = 0; ai < anios.size(); ai++) {
            int a = anios.get(ai);
            if (ai > 0) evoJs.append(",");
            evoJs.append(a).append(":[");
            for (int li = 0; li < evoLabels.size(); li++) {
                String lbl = evoLabels.get(li);
                double[] precios = evoDataMap.get(lbl) != null && evoDataMap.get(lbl).containsKey(a)
                    ? evoDataMap.get(lbl).get(a) : new double[12];
                String color = linePal[li % linePal.length];
                if (li > 0) evoJs.append(",");
                evoJs.append("{label:'").append(escapeJS(lbl))
                     .append("',borderColor:'").append(color)
                     .append("',backgroundColor:'").append(color)
                     .append("',tension:0.3,fill:false,spanGaps:true,pointRadius:4,data:[");
                for (int mi = 0; mi < 12; mi++) {
                    if (mi > 0) evoJs.append(",");
                    if (precios[mi] == 0) evoJs.append("null");
                    else evoJs.append(String.format(Locale.US,"%.3f", precios[mi] / 100.0));
                }
                evoJs.append("]}");
            }
            evoJs.append("]");
        }
        evoJs.append("}");

        // Selector de año (sólo visible en vista evolución)
        StringBuilder anioOptions = new StringBuilder();
        for (int a : anios) {
            anioOptions.append("<option value='").append(a).append("'")
                       .append(a == anioDefault ? " selected" : "").append(">").append(a).append("</option>");
        }

        return "<div class='card' style='margin-bottom:24px'>\n"
            + "  <div style='padding:14px 20px 10px;display:flex;align-items:center;justify-content:space-between;flex-wrap:wrap;gap:8px'>\n"
            + "    <div style='display:flex;align-items:center;gap:10px'>\n"
            + "      <span style='font-size:20px'>📊</span>\n"
            + "      <div>\n"
            + "        <h3 id='excel-chart-title' style='margin:0;font-size:15px;color:#1b4332'>Precio medio por producto (€/kg)</h3>\n"
            + "        <span style='font-size:11px;color:#78909C'>Ministerio de Agricultura · Datos 2024–2026</span>\n"
            + "      </div>\n"
            + "    </div>\n"
            + "    <div style='display:flex;gap:6px;align-items:center'>\n"
            + "      <select id='anio-select' onchange='excelCambiarAnio()' style='display:none;padding:4px 10px;border:1.5px solid #D0D8E4;border-radius:8px;font-size:12px;font-weight:600;background:white;color:#1A2332;cursor:pointer'>"
            + anioOptions + "</select>\n"
            + "      <button id='btn-promedio' onclick='excelVista(\"promedio\")' style='padding:5px 14px;border:1.5px solid #2E7D32;border-radius:20px;font-size:12px;font-weight:600;cursor:pointer;background:#2E7D32;color:white'>Promedio</button>\n"
            + (hayEvo ? "      <button id='btn-evolucion' onclick='excelVista(\"evolucion\")' style='padding:5px 14px;border:1.5px solid #D0D8E4;border-radius:20px;font-size:12px;font-weight:600;cursor:pointer;background:white;color:#5A6779'>Evolución mensual</button>\n" : "")
            + "    </div>\n"
            + "  </div>\n"
            + "  <div style='padding:4px 20px 20px;height:340px'>\n"
            + "    <canvas id='excel-chart'></canvas>\n"
            + "  </div>\n"
            + "</div>\n"
            + "<script>\n"
            + "(function(){\n"
            + "  var barLabels=" + barLabels + ";\n"
            + "  var barValues=" + barValues + ";\n"
            + "  var barColors=" + barColors + ";\n"
            + "  var meses=" + mesesJs + ";\n"
            + "  var evoData=" + evoJs + ";\n"
            + "  var anioActual=" + anioDefault + ";\n"
            + "  var chart = new Chart(document.getElementById('excel-chart'),{\n"
            + "    type:'bar',\n"
            + "    data:{labels:barLabels,datasets:[{label:'€/kg',data:barValues,backgroundColor:barColors,borderRadius:4}]},\n"
            + "    options:{\n"
            + "      responsive:true,maintainAspectRatio:false,\n"
            + "      plugins:{legend:{display:false},tooltip:{callbacks:{label:function(c){return ' €'+parseFloat(c.raw).toFixed(2)+'/kg';}}}},\n"
            + "      scales:{y:{beginAtZero:false,ticks:{callback:function(v){return '€'+v.toFixed(2);}}}}\n"
            + "    }\n"
            + "  });\n"
            + "  window.excelVista = function(vista){\n"
            + "    var btnP=document.getElementById('btn-promedio');\n"
            + "    var btnE=document.getElementById('btn-evolucion');\n"
            + "    var sel=document.getElementById('anio-select');\n"
            + "    var titulo=document.getElementById('excel-chart-title');\n"
            + "    if(vista==='promedio'){\n"
            + "      btnP.style.background='#2E7D32';btnP.style.color='white';btnP.style.borderColor='#2E7D32';\n"
            + "      if(btnE){btnE.style.background='white';btnE.style.color='#5A6779';btnE.style.borderColor='#D0D8E4';}\n"
            + "      sel.style.display='none';\n"
            + "      titulo.textContent='Precio medio por producto (€/kg)';\n"
            + "      chart.config.type='bar';\n"
            + "      chart.config.data={labels:barLabels,datasets:[{label:'€/kg',data:barValues,backgroundColor:barColors,borderRadius:4}]};\n"
            + "      chart.config.options.plugins.legend.display=false;\n"
            + "    } else {\n"
            + "      if(btnE){btnE.style.background='#1565C0';btnE.style.color='white';btnE.style.borderColor='#1565C0';}\n"
            + "      btnP.style.background='white';btnP.style.color='#5A6779';btnP.style.borderColor='#D0D8E4';\n"
            + "      sel.style.display='inline-block';\n"
            + "      titulo.textContent='Evolución mensual por producto (€/kg)';\n"
            + "      chart.config.type='line';\n"
            + "      chart.config.data={labels:meses,datasets:evoData[anioActual]||[]};\n"
            + "      chart.config.options.plugins.legend.display=true;\n"
            + "    }\n"
            + "    chart.update();\n"
            + "  };\n"
            + "  window.excelCambiarAnio = function(){\n"
            + "    anioActual=parseInt(document.getElementById('anio-select').value);\n"
            + "    chart.config.data={labels:meses,datasets:evoData[anioActual]||[]};\n"
            + "    chart.update();\n"
            + "  };\n"
            + "})();\n"
            + "</script>\n";
    }

    // ─── Gráfica Chart.js ─────────────────────────────────────────────────────

    private static String construirGrafica(List<Map<String, Object>> productos) {
        StringBuilder rawDataJs = new StringBuilder("[");
        boolean firstItem = true;
        Set<String> clavesVistas = new java.util.HashSet<>();

        for (Map<String, Object> p : productos) {
            String nombre   = canonico(p.getOrDefault("nombre", "").toString().trim());
            Object varObj   = p.get("variedad");
            String variedad = (varObj != null && !varObj.toString().trim().isEmpty())
                              ? varObj.toString().trim() : "";
            String clave    = variedad.isEmpty() ? nombre : nombre + " · " + variedad;
            String origen   = p.getOrDefault("origen", "BD").toString().toUpperCase();
            String fecha    = p.getOrDefault("fecha_actualizacion", "").toString();
            double precio;
            try { precio = ((Number) p.getOrDefault("precio", 0.0)).doubleValue(); }
            catch (Exception e) { precio = 0; }
            if (precio <= 0 || clave.isEmpty()) continue;

            clavesVistas.add(clave);
            if (!firstItem) rawDataJs.append(",");
            firstItem = false;
            rawDataJs.append("{k:'").append(escapeJS(clave))
                     .append("',v:").append(String.format(Locale.US, "%.2f", precio))
                     .append(",o:'").append(escapeJS(origen))
                     .append("',d:'").append(escapeJS(fecha.length() >= 10 ? fecha.substring(0, 10) : fecha))
                     .append("'}");
        }
        rawDataJs.append("]");

        if (clavesVistas.isEmpty()) return "";

        String nota = clavesVistas.size() + " grupos · precio medio";

        return "<div class='card'>\n"
            + "  <div class='card-head'><h2>📊 Precio Medio por Producto</h2><span class='card-note'>" + nota + "</span></div>\n"
            + "  <div class='period-btns'>\n"
            // Botones de periodo para el gráfico de barras
            + "    <button class='period-btn active' data-period='todo'>Todo</button>\n"
            + "    <button class='period-btn' data-period='7d'>7 días</button>\n"
            + "    <button class='period-btn' data-period='30d'>30 días</button>\n"
            + "    <span class='period-sep'></span>\n"
            // Botón evolución
            + "    <button class='period-btn evo-btn' id='btn-evo'>📈 Evolución</button>\n"
            + "  </div>\n"
            + "  <div class='chart-wrap'><canvas id='gchart'></canvas></div>\n"
            + "  <div class='chart-legend' id='legend-bar'>\n"
            + "    <span class='legend-item'><span class='legend-dot' style='background:#1565C0'></span>AgroPrecios.com</span>\n"
            + "    <span class='legend-item'><span class='legend-dot' style='background:#E64A19'></span>AgroPizarra.com</span>\n"
            + "  </div>\n"
            + "  <div class='chart-legend' id='legend-evo' style='display:none'>\n"
            + "    <span style='font-size:12px;color:#78909C'>Precio medio (€/kg) por fecha — top 8 productos</span>\n"
            + "  </div>\n"
            + "  <script>\n"
            + "  (function(){\n"
            + "    var rawData=" + rawDataJs + ";\n"
            + "    var palette=['#2E7D32','#1565C0','#E64A19','#7B1FA2','#F57F17','#00838F','#4E342E','#546E7A'];\n"
            + "    function clr(o,a){if(o==='AGROPRECIOS')return 'rgba(21,101,192,'+a+')';if(o==='AGROPIZARRA')return 'rgba(230,74,25,'+a+')';return 'rgba(46,125,50,'+a+')';}\n"

            // ── Gráfico de barras (por producto) ──
            + "    function computeBar(items){\n"
            + "      var ap={},az={};\n"
            + "      items.forEach(function(d){\n"
            + "        if(!d.v||d.v<=0||!d.k)return;\n"
            + "        if(d.o==='AGROPRECIOS'){if(!ap[d.k])ap[d.k]={s:0,n:0};ap[d.k].s+=d.v;ap[d.k].n++;}\n"
            + "        else if(d.o==='AGROPIZARRA'){if(!az[d.k])az[d.k]={s:0,n:0};az[d.k].s+=d.v;az[d.k].n++;}\n"
            + "      });\n"
            + "      var all={};\n"
            + "      [ap,az].forEach(function(src){Object.keys(src).forEach(function(k){all[k]=(ap[k]?ap[k].n:0)+(az[k]?az[k].n:0);});});\n"
            + "      var ks=Object.keys(all).sort(function(a,b){return all[b]-all[a];});\n"
            + "      if(ks.length>25)ks=ks.slice(0,25);\n"
            + "      ks.sort();\n"
            + "      var dAP=ks.map(function(k){return ap[k]?parseFloat((ap[k].s/ap[k].n).toFixed(2)):null;});\n"
            + "      var dAZ=ks.map(function(k){return az[k]?parseFloat((az[k].s/az[k].n).toFixed(2)):null;});\n"
            + "      return {labels:ks,dAP:dAP,dAZ:dAZ};\n"
            + "    }\n"
            + "    function mkDatasets(cd){\n"
            + "      return [\n"
            + "        {label:'AgroPrecios.com',data:cd.dAP,backgroundColor:'rgba(21,101,192,0.75)',borderColor:'rgba(21,101,192,1)',borderWidth:1.5,borderRadius:5,borderSkipped:false},\n"
            + "        {label:'AgroPizarra.com',data:cd.dAZ,backgroundColor:'rgba(230,74,25,0.75)',borderColor:'rgba(230,74,25,1)',borderWidth:1.5,borderRadius:5,borderSkipped:false}\n"
            + "      ];\n"
            + "    }\n"

            // ── Gráfico de líneas (evolución por fecha) ──
            + "    function computeEvo(items){\n"
            + "      var dates=[...new Set(items.map(function(d){return d.d||'';}).filter(Boolean))].sort();\n"
            + "      var pc={};\n"
            + "      items.forEach(function(d){if(d.k&&d.v>0&&d.d){if(!pc[d.k])pc[d.k]=new Set();pc[d.k].add(d.d);}});\n"
            + "      var top=Object.keys(pc).sort(function(a,b){return pc[b].size-pc[a].size;}).slice(0,8).sort();\n"
            + "      var datasets=top.map(function(prod,i){\n"
            + "        var data=dates.map(function(date){\n"
            + "          var pts=items.filter(function(d){return d.k===prod&&d.v>0&&d.d===date;});\n"
            + "          return pts.length?parseFloat((pts.reduce(function(s,d){return s+d.v;},0)/pts.length).toFixed(2)):null;\n"
            + "        });\n"
            + "        return {label:prod,data:data,borderColor:palette[i%8],backgroundColor:palette[i%8]+'30',fill:false,tension:0.3,pointRadius:5,pointHoverRadius:7,spanGaps:true};\n"
            + "      });\n"
            + "      return {labels:dates,datasets:datasets};\n"
            + "    }\n"

            // ── Filtro por periodo ──
            + "    function filterPeriod(p){\n"
            + "      if(p==='todo')return rawData;\n"
            + "      var days=p==='7d'?7:30;\n"
            + "      var cutoff=new Date();cutoff.setDate(cutoff.getDate()-days);\n"
            + "      var cutStr=cutoff.toISOString().substring(0,10);\n"
            + "      return rawData.filter(function(d){return !d.d||d.d>=cutStr;});\n"
            + "    }\n"

            // ── Inicializar gráfico de barras ──
            + "    var modoEvo=false;\n"
            + "    var cd=computeBar(rawData);\n"
            + "    var chart=new Chart(document.getElementById('gchart'),{\n"
            + "      type:'bar',\n"
            + "      data:{labels:cd.labels,datasets:mkDatasets(cd)},\n"
            + "      options:{\n"
            + "        responsive:true,maintainAspectRatio:false,\n"
            + "        plugins:{legend:{display:false},tooltip:{callbacks:{label:function(c){return ' '+c.dataset.label+': €'+(c.raw!==null?parseFloat(c.raw).toFixed(2):'—')+'/kg';}}}},\n"
            + "        scales:{y:{beginAtZero:true,grid:{color:'#EEF2F7'},ticks:{callback:function(v){return '€'+v.toFixed(2);}}},x:{grid:{display:false},ticks:{maxRotation:40,minRotation:20,font:{size:11}}}}\n"
            + "      }\n"
            + "    });\n"

            // ── Botones de periodo ──
            + "    document.querySelectorAll('.period-btn:not(.evo-btn)').forEach(function(btn){\n"
            + "      btn.addEventListener('click',function(){\n"
            + "        if(modoEvo)return;\n"
            + "        document.querySelectorAll('.period-btn:not(.evo-btn)').forEach(function(b){b.classList.remove('active');});\n"
            + "        this.classList.add('active');\n"
            + "        var filtered=filterPeriod(this.dataset.period);\n"
            + "        var cd=computeBar(filtered);\n"
            + "        chart.data.labels=cd.labels;\n"
            + "        chart.data.datasets=mkDatasets(cd);\n"
            + "        chart.config.type='bar';\n"
            + "        chart.update();\n"
            + "      });\n"
            + "    });\n"

            // ── Botón evolución (toggle) ──
            + "    document.getElementById('btn-evo').addEventListener('click',function(){\n"
            + "      modoEvo=!modoEvo;\n"
            + "      this.classList.toggle('active',modoEvo);\n"
            + "      document.getElementById('legend-bar').style.display=modoEvo?'none':'flex';\n"
            + "      document.getElementById('legend-evo').style.display=modoEvo?'flex':'none';\n"
            + "      document.querySelectorAll('.period-btn:not(.evo-btn)').forEach(function(b){b.disabled=modoEvo;b.style.opacity=modoEvo?'0.4':'1';});\n"
            + "      if(modoEvo){\n"
            + "        var ed=computeEvo(rawData);\n"
            + "        chart.data.labels=ed.labels;\n"
            + "        chart.data.datasets=ed.datasets;\n"
            + "        chart.config.type='line';\n"
            + "        chart.options.plugins.legend={display:true,position:'bottom',labels:{font:{size:11},boxWidth:14}};\n"
            + "        chart.options.plugins.tooltip={callbacks:{label:function(c){return ' '+c.dataset.label+': €'+parseFloat(c.raw).toFixed(2)+'/kg';}}};\n"
            + "        chart.update();\n"
            + "      } else {\n"
            + "        var activeBtn=document.querySelector('.period-btn:not(.evo-btn).active');\n"
            + "        var period=activeBtn?activeBtn.dataset.period:'todo';\n"
            + "        var cd=computeBar(filterPeriod(period));\n"
            + "        chart.data.labels=cd.labels;\n"
            + "        chart.data.datasets=mkDatasets(cd);\n"
            + "        chart.config.type='bar';\n"
            + "        chart.options.plugins.legend={display:false};\n"
            + "        chart.options.plugins.tooltip={callbacks:{label:function(c){return ' €'+parseFloat(c.raw).toFixed(2)+'/kg (media)';}}};\n"
            + "        chart.update();\n"
            + "      }\n"
            + "    });\n"
            + "  })();\n"
            + "  </script>\n"
            + "</div>\n";
    }

    // ─── Tabla ────────────────────────────────────────────────────────────────

    private static String construirTabla(List<Map<String, Object>> productos) {
        // Nivel 1: producto canónico → Nivel 2: variedad → registros
        Map<String, Map<String, List<Map<String, Object>>>> grupos = new LinkedHashMap<>();
        for (Map<String, Object> p : productos) {
            String nombre = canonico(p.getOrDefault("nombre", "").toString().trim());
            Object varObj = p.get("variedad");
            String variedad = (varObj != null && !varObj.toString().trim().isEmpty()) ? varObj.toString().trim() : "";
            grupos.computeIfAbsent(nombre, k -> new LinkedHashMap<>())
                  .computeIfAbsent(variedad, k -> new ArrayList<>())
                  .add(p);
        }

        // Precio medio por producto para colorear filas (verde=barato, rojo=caro)
        Map<String, Double> avgPrecio = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, List<Map<String, Object>>>> e : grupos.entrySet()) {
            OptionalDouble avg = e.getValue().values().stream()
                .flatMap(List::stream)
                .mapToDouble(p -> { try { return ((Number) p.getOrDefault("precio", 0.0)).doubleValue(); } catch (Exception ex) { return 0; } })
                .filter(v -> v > 0)
                .average();
            avgPrecio.put(e.getKey(), avg.orElse(0.0));
        }

        StringBuilder t = new StringBuilder();
        t.append("<div class='card'>\n");
        t.append("  <div class='card-head'>\n");
        t.append("    <h2>📋 Listado de Precios</h2>\n");
        t.append("    <div style='display:flex;align-items:center;gap:10px;'>\n");
        t.append("      <span class='card-note'>").append(grupos.size()).append(" productos · ").append(productos.size()).append(" registros</span>\n");
        t.append("      <div class='tbl-actions'>\n");
        t.append("        <button class='btn-xs' id='btn-expand-all'>▼ Expandir todo</button>\n");
        t.append("        <button class='btn-xs' id='btn-collapse-all'>▲ Colapsar todo</button>\n");
        t.append("      </div>\n");
        t.append("    </div>\n");
        t.append("  </div>\n");
        t.append("  <table>\n");
        t.append("    <thead><tr>");
        t.append("<th>Producto</th><th></th><th>Fuente</th><th>Precio/kg</th><th>Origen</th><th>Fecha</th>");
        t.append("</tr></thead>\n");
        t.append("    <tbody>\n");

        int gi = 0, sgi = 0;
        for (Map.Entry<String, Map<String, List<Map<String, Object>>>> entry : grupos.entrySet()) {
            String nombre = entry.getKey();
            Map<String, List<Map<String, Object>>> varGrupos = entry.getValue();
            String gid = "g" + gi++;
            String icono = iconoProducto(nombre);
            int totalReg = varGrupos.values().stream().mapToInt(List::size).sum();

            double minP = varGrupos.values().stream().flatMap(List::stream).mapToDouble(p -> {
                try { return ((Number) p.getOrDefault("precio", 0.0)).doubleValue(); } catch (Exception e) { return 0; }
            }).filter(v -> v > 0).min().orElse(0);
            double maxP = varGrupos.values().stream().flatMap(List::stream).mapToDouble(p -> {
                try { return ((Number) p.getOrDefault("precio", 0.0)).doubleValue(); } catch (Exception e) { return 0; }
            }).filter(v -> v > 0).max().orElse(0);

            // Cabecera de producto (nivel 1)
            t.append("      <tr class='group-header' data-group='").append(gid).append("'>\n");
            t.append("        <td colspan='2'><span class='toggle-icon'>▶</span><span class='prod-icon'>").append(icono).append("</span>").append(escapeHTML(nombre)).append("</td>\n");
            t.append("        <td class='group-count'>").append(totalReg).append(" reg · ").append(varGrupos.size()).append(" var</td>\n");
            t.append("        <td class='precio'>€").append(String.format(Locale.US, "%.2f", minP));
            if (maxP > minP) t.append(" – €").append(String.format(Locale.US, "%.2f", maxP));
            t.append("</td>\n");
            t.append("        <td></td><td></td>\n");
            t.append("      </tr>\n");

            for (Map.Entry<String, List<Map<String, Object>>> varEntry : varGrupos.entrySet()) {
                String variedad = varEntry.getKey();
                List<Map<String, Object>> filas = varEntry.getValue();
                String sgid = "sg" + sgi++;

                double vMin = filas.stream().mapToDouble(p -> {
                    try { return ((Number) p.getOrDefault("precio", 0.0)).doubleValue(); } catch (Exception e) { return 0; }
                }).filter(v -> v > 0).min().orElse(0);
                double vMax = filas.stream().mapToDouble(p -> {
                    try { return ((Number) p.getOrDefault("precio", 0.0)).doubleValue(); } catch (Exception e) { return 0; }
                }).filter(v -> v > 0).max().orElse(0);
                String varLabel = variedad.isEmpty() ? "<em style='color:#A0AABB'>sin variedad</em>" : escapeHTML(variedad);

                // Cabecera de variedad (nivel 2)
                t.append("      <tr class='subgroup-header' data-group='").append(gid).append("' data-subgroup='").append(sgid).append("'>\n");
                t.append("        <td colspan='2'><span class='sub-toggle'>▶</span>").append(varLabel).append("</td>\n");
                t.append("        <td class='group-count'>").append(filas.size()).append(" reg</td>\n");
                t.append("        <td class='precio'>€").append(String.format(Locale.US, "%.2f", vMin));
                if (vMax > vMin) t.append(" – €").append(String.format(Locale.US, "%.2f", vMax));
                t.append("</td>\n");
                t.append("        <td></td><td></td>\n");
                t.append("      </tr>\n");

                // Filas de datos
                for (Map<String, Object> p : filas) {
                    String origen = p.getOrDefault("origen", "BD").toString().toUpperCase();
                    String ornCls, ornLabel, rowCls;
                    switch (origen) {
                        case "AGROPRECIOS": ornCls = "orn-agroprecios"; ornLabel = "🌐 AgroPrecios"; rowCls = "row-agroprecios"; break;
                        case "AGROPIZARRA": ornCls = "orn-agropizarra"; ornLabel = "📊 AgroPizarra"; rowCls = "row-agropizarra"; break;
                        default:            ornCls = "orn-bd";          ornLabel = "🗄️ BD";          rowCls = "row-bd";
                    }
                    double precio;
                    try { precio = ((Number) p.getOrDefault("precio", 0.0)).doubleValue(); } catch (Exception e) { precio = 0.0; }
                    String fecha = p.getOrDefault("fecha_actualizacion", "").toString();

                    double avg = avgPrecio.getOrDefault(nombre, 0.0);
                    String pClass = "precio";
                    String pTitle = "";
                    if (avg > 0 && precio > 0) {
                        if (precio < avg - 0.005) {
                            pClass = "precio precio-bajo";
                            pTitle = " title='Por debajo de la media (€" + String.format(Locale.US, "%.2f", avg) + "/kg)'";
                        } else if (precio > avg + 0.005) {
                            pClass = "precio precio-alto";
                            pTitle = " title='Por encima de la media (€" + String.format(Locale.US, "%.2f", avg) + "/kg)'";
                        }
                    }
                    t.append("      <tr class='group-child ").append(rowCls).append("' data-group='").append(gid).append("' data-subgroup='").append(sgid).append("'>\n");
                    t.append("        <td class='child-indent'>└</td>\n");
                    t.append("        <td></td>\n");
                    t.append("        <td>").append(escapeHTML(p.getOrDefault("fuente", "").toString())).append("</td>\n");
                    t.append("        <td class='").append(pClass).append("'").append(pTitle).append(">€").append(String.format(Locale.US, "%.2f", precio)).append("</td>\n");
                    t.append("        <td><span class='orn ").append(ornCls).append("'>").append(ornLabel).append("</span></td>\n");
                    t.append("        <td class='fecha-cell'>").append(escapeHTML(formatearFecha(fecha))).append("</td>\n");
                    t.append("      </tr>\n");
                }
            }
        }

        t.append("    </tbody>\n  </table>\n");
        t.append("  <script>\n");
        t.append("  (function(){\n");
        // Clic en cabecera de producto: muestra/oculta subgrupos de variedad
        t.append("    document.querySelectorAll('.group-header').forEach(function(h){\n");
        t.append("      h.addEventListener('click',function(){\n");
        t.append("        var g=this.dataset.group,open=this.classList.contains('open');\n");
        t.append("        this.classList.toggle('open');\n");
        t.append("        document.querySelectorAll('.subgroup-header[data-group=\"'+g+'\"]').forEach(function(sh){\n");
        t.append("          sh.style.display=open?'none':'table-row';\n");
        t.append("          if(open){sh.classList.remove('open');document.querySelectorAll('.group-child[data-subgroup=\"'+sh.dataset.subgroup+'\"]').forEach(function(r){r.style.display='none';});}\n");
        t.append("        });\n");
        t.append("      });\n");
        t.append("    });\n");
        // Clic en cabecera de variedad: muestra/oculta filas de datos
        t.append("    document.querySelectorAll('.subgroup-header').forEach(function(h){\n");
        t.append("      h.addEventListener('click',function(e){\n");
        t.append("        e.stopPropagation();\n");
        t.append("        var sg=this.dataset.subgroup,open=this.classList.contains('open');\n");
        t.append("        this.classList.toggle('open');\n");
        t.append("        document.querySelectorAll('.group-child[data-subgroup=\"'+sg+'\"]').forEach(function(r){\n");
        t.append("          r.style.display=open?'none':'table-row';\n");
        t.append("        });\n");
        t.append("      });\n");
        t.append("    });\n");
        // Expandir todo
        t.append("    document.getElementById('btn-expand-all').addEventListener('click',function(){\n");
        t.append("      document.querySelectorAll('.group-header').forEach(function(h){h.classList.add('open');});\n");
        t.append("      document.querySelectorAll('.subgroup-header').forEach(function(h){h.style.display='table-row';h.classList.add('open');});\n");
        t.append("      document.querySelectorAll('.group-child').forEach(function(r){r.style.display='table-row';});\n");
        t.append("    });\n");
        // Colapsar todo
        t.append("    document.getElementById('btn-collapse-all').addEventListener('click',function(){\n");
        t.append("      document.querySelectorAll('.group-header').forEach(function(h){h.classList.remove('open');});\n");
        t.append("      document.querySelectorAll('.subgroup-header').forEach(function(h){h.style.display='none';h.classList.remove('open');});\n");
        t.append("      document.querySelectorAll('.group-child').forEach(function(r){r.style.display='none';});\n");
        t.append("    });\n");
        t.append("  })();\n");
        t.append("  </script>\n");
        t.append("</div>\n");
        return t.toString();
    }

    private static String formatearFecha(String fecha) {
        if (fecha == null || fecha.trim().isEmpty()) return "—";
        try {
            String f = fecha.trim();
            if (f.length() >= 10) {
                String[] d = f.substring(0, 10).split("-");
                if (d.length == 3) {
                    String resultado = d[2] + "/" + d[1] + "/" + d[0];
                    if (f.length() >= 16) resultado += " " + f.substring(11, 16);
                    return resultado;
                }
            }
        } catch (Exception e) {}
        return fecha.substring(0, Math.min(16, fecha.length()));
    }

    private static String iconoProducto(String nombre) {
        if (nombre == null) return "🌿";
        String n = nombre.toLowerCase()
            .replace("á","a").replace("é","e").replace("í","i")
            .replace("ó","o").replace("ú","u").replace("ü","u").replace("ñ","n");
        if (n.contains("tomate"))                                                   return "🍅";
        if (n.contains("naranja"))                                                  return "🍊";
        if (n.contains("limon"))                                                    return "🍋";
        if (n.contains("manzana"))                                                  return "🍎";
        if (n.contains("pera") && !n.contains("peral"))                            return "🍐";
        if (n.contains("uva") || n.contains("vid"))                                return "🍇";
        if (n.contains("fresa") || n.contains("freson"))                           return "🍓";
        if (n.contains("melocoton") || n.contains("nectarina"))                    return "🍑";
        if (n.contains("sandia"))                                                   return "🍉";
        if (n.contains("melon") && !n.contains("melocoton"))                       return "🍈";
        if (n.contains("cereza"))                                                   return "🍒";
        if (n.contains("kiwi"))                                                     return "🥝";
        if (n.contains("platano") || n.contains("banana"))                         return "🍌";
        if (n.contains("mango"))                                                    return "🥭";
        if (n.contains("pina") || n.contains("ananas"))                            return "🍍";
        if (n.contains("pimiento") || n.contains("guindilla"))                     return "🌶️";
        if (n.contains("zanahoria"))                                                return "🥕";
        if (n.contains("lechuga") || n.contains("escarola")
            || n.contains("espinaca") || n.contains("acelga"))                     return "🥬";
        if (n.contains("cebolla"))                                                  return "🧅";
        if (n.contains("ajo"))                                                      return "🧄";
        if (n.contains("patata") || n.contains("papa"))                            return "🥔";
        if (n.contains("calabacin") || n.contains("pepino"))                       return "🥒";
        if (n.contains("brocoli") || n.contains("coliflor") || n.contains("col")) return "🥦";
        if (n.contains("maiz"))                                                     return "🌽";
        if (n.contains("berenjena"))                                                return "🍆";
        if (n.contains("aguacate") || n.contains("palta"))                         return "🥑";
        if (n.contains("garbanzo") || n.contains("alubia") || n.contains("judia")
            || n.contains("lenteja") || n.contains("haba") || n.contains("guisante")) return "🫘";
        if (n.contains("trigo") || n.contains("cebada") || n.contains("avena")
            || n.contains("centeno"))                                               return "🌾";
        if (n.contains("arroz"))                                                    return "🍚";
        if (n.contains("aceituna") || n.contains("oliva"))                         return "🫒";
        if (n.contains("champinon") || n.contains("seta"))                         return "🍄";
        if (n.contains("almendra") || n.contains("nuez")
            || n.contains("avellana") || n.contains("pistach"))                    return "🥜";
        if (n.contains("apio") || n.contains("perejil") || n.contains("cilantro")) return "🌿";
        return "🌿";
    }

    // ─── Utilidades ──────────────────────────────────────────────────────────

    private static String escapeHTML(String texto) {
        if (texto == null) return "";
        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                    .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private static String escapeJS(String texto) {
        if (texto == null) return "";
        return texto.replace("\\", "\\\\").replace("'", "\\'");
    }

    // ─── Modal de carga ──────────────────────────────────────────────────────

    private static String construirModalCarga(List<String> fechasConDatos) {
        List<String> diasHabiles = new ArrayList<>();
        LocalDate dia = LocalDate.now();
        while (diasHabiles.size() < 10) {
            if (dia.getDayOfWeek() != DayOfWeek.SUNDAY) {
                diasHabiles.add(dia.toString());
            }
            dia = dia.minusDays(1);
        }

        Set<String> cargadas = new HashSet<>(fechasConDatos);
        Set<String> enProceso = new HashSet<>();
        try { enProceso = new HashSet<>(ProductoService.obtenerFechasEnProceso()); } catch (Exception ignored) {}
        String hoy = LocalDate.now().toString();

        StringBuilder sb = new StringBuilder();
        sb.append("<div id='modal-carga' class='modal-overlay' onclick=\"if(event.target===this)this.style.display='none'\">\n");
        sb.append("  <div class='modal-box'>\n");
        sb.append("    <div class='modal-title'>⬇️ Cargar datos del scraper</div>\n");
        sb.append("    <div class='modal-sub'>Selecciona el día de mercado (últimos 10 días hábiles):</div>\n");
        sb.append("    <form action='/cargar' method='GET'>\n");
        sb.append("      <div style='display:flex;flex-direction:column;gap:7px;max-height:330px;overflow-y:auto;padding-right:4px;'>\n");

        boolean primerDisponible = true;
        for (String fechaDia : diasHabiles) {
            boolean enCurso = enProceso.contains(fechaDia);
            boolean cargado = !enCurso && cargadas.contains(fechaDia);
            String fmt = formatearFechaCorta(fechaDia);
            String sufijo = fechaDia.equals(hoy) ? " — hoy" : "";
            String clsExtra = cargado ? " dia-cargado" : (enCurso ? " dia-en-proceso" : "");

            sb.append("        <label class='dia-option").append(clsExtra).append("'>\n");
            sb.append("          <div class='dia-label'>\n");
            if (cargado || enCurso) {
                sb.append("            <input type='radio' name='fecha' value='").append(fechaDia).append("' disabled>\n");
            } else {
                String checked = primerDisponible ? " checked" : "";
                sb.append("            <input type='radio' name='fecha' value='").append(fechaDia).append("'").append(checked).append(">\n");
                primerDisponible = false;
            }
            sb.append("            <span>📅 ").append(escapeHTML(fmt)).append(escapeHTML(sufijo)).append("</span>\n");
            sb.append("          </div>\n");
            if (enCurso)
                sb.append("          <span class='dia-badge dia-badge-proc'>⏳ en proceso</span>\n");
            else if (cargado)
                sb.append("          <span class='dia-badge dia-badge-ok'>✓ cargado</span>\n");
            else
                sb.append("          <span class='dia-badge dia-badge-new'>disponible</span>\n");
            sb.append("        </label>\n");
        }

        sb.append("      </div>\n");
        sb.append("      <div style='display:flex;gap:8px;margin-top:20px;justify-content:flex-end;'>\n");
        sb.append("        <button type='button' onclick=\"document.getElementById('modal-carga').style.display='none'\" class='btn btn-ghost'>Cancelar</button>\n");
        sb.append("        <button type='submit' class='btn btn-blue'>⬇️ Cargar</button>\n");
        sb.append("      </div>\n");
        sb.append("    </form>\n");
        sb.append("  </div>\n");
        sb.append("</div>\n");
        return sb.toString();
    }

    // ─── Modal de vaciado ────────────────────────────────────────────────────

    private static String construirModalVaciar(List<String> fechasConDatos) {
        List<String> diasHabiles = new ArrayList<>();
        LocalDate dia = LocalDate.now();
        while (diasHabiles.size() < 10) {
            if (dia.getDayOfWeek() != DayOfWeek.SUNDAY) {
                diasHabiles.add(dia.toString());
            }
            dia = dia.minusDays(1);
        }
        Set<String> cargadas = new HashSet<>(fechasConDatos);
        String hoy = LocalDate.now().toString();
        boolean requiereClave = !System.getenv().getOrDefault("ADMIN_PASSWORD", "").isEmpty();

        StringBuilder sb = new StringBuilder();
        sb.append("<div id='modal-vaciar' class='modal-overlay' onclick=\"if(event.target===this)this.style.display='none'\">\n");
        sb.append("  <div class='modal-box'>\n");
        sb.append("    <div class='modal-title'>🗑️ Vaciar datos del scraper</div>\n");
        sb.append("    <div class='modal-sub'>Elige qué datos eliminar de la base de datos:</div>\n");

        if (requiereClave) {
            sb.append("    <div style='margin-bottom:14px;'>\n");
            sb.append("      <label style='font-size:12px;color:#78909C;display:block;margin-bottom:5px;font-weight:600;'>🔒 Contraseña de administrador</label>\n");
            sb.append("      <input type='password' id='vaciar-clave' placeholder='Contraseña requerida'\n");
            sb.append("        style='width:100%;padding:9px 12px;border:1.5px solid #FFCDD2;border-radius:8px;font-size:14px;outline:none;'\n");
            sb.append("        onfocus=\"this.style.borderColor='#C62828'\" onblur=\"this.style.borderColor='#FFCDD2'\">\n");
            sb.append("    </div>\n");
        }

        sb.append("    <div style='display:flex;flex-direction:column;gap:7px;max-height:270px;overflow-y:auto;padding-right:4px;margin-bottom:14px;'>\n");

        for (String fechaDia : diasHabiles) {
            boolean tieneDatos = cargadas.contains(fechaDia);
            String fmt = formatearFechaCorta(fechaDia);
            String sufijo = fechaDia.equals(hoy) ? " — hoy" : "";
            String clsExtra = tieneDatos ? " dia-vaciar" : " dia-sin-datos";

            sb.append("      <label class='dia-option").append(clsExtra).append("'>\n");
            sb.append("        <div class='dia-label'><span>📅 ")
              .append(escapeHTML(fmt)).append(escapeHTML(sufijo)).append("</span></div>\n");
            if (tieneDatos) {
                sb.append("        <button type='button' class='btn btn-danger' style='padding:4px 12px;font-size:12px;'\n");
                sb.append("          onclick=\"vaciarConClave('").append(fechaDia).append("','").append(escapeJS(fmt)).append("')\">")
                  .append("🗑️ Vaciar</button>\n");
            } else {
                sb.append("        <span class='dia-badge dia-badge-proc' style='background:#F5F5F5;color:#BBBBBB;'>sin datos</span>\n");
            }
            sb.append("      </label>\n");
        }

        sb.append("    </div>\n");
        sb.append("    <div style='display:flex;gap:8px;justify-content:space-between;align-items:center;border-top:1px solid #EEF2F7;padding-top:14px;'>\n");
        sb.append("      <button type='button' class='btn btn-danger' onclick=\"vaciarConClave('','todo')\">🗑️ Vaciar todo</button>\n");
        sb.append("      <button type='button' onclick=\"document.getElementById('modal-vaciar').style.display='none'\" class='btn btn-ghost'>Cerrar</button>\n");
        sb.append("    </div>\n");
        sb.append("  </div>\n");
        sb.append("</div>\n");
        sb.append("<script>\n");
        sb.append("function vaciarConClave(fecha, label) {\n");
        if (requiereClave) {
            sb.append("  var clave = document.getElementById('vaciar-clave').value;\n");
            sb.append("  if (!clave) { alert('Introduce la contraseña de administrador.'); return; }\n");
        } else {
            sb.append("  var clave = '';\n");
        }
        sb.append("  var msg = fecha ? '¿Vaciar datos del ' + label + '?' : '¿Vaciar TODOS los datos del scraper?';\n");
        sb.append("  if (!confirm(msg)) return;\n");
        sb.append("  var url = '/vaciar';\n");
        sb.append("  var sep = '?';\n");
        sb.append("  if (fecha) { url += sep + 'fecha=' + encodeURIComponent(fecha); sep = '&'; }\n");
        sb.append("  if (clave) { url += sep + 'clave=' + encodeURIComponent(clave); }\n");
        sb.append("  window.location.href = url;\n");
        sb.append("}\n");
        sb.append("</script>\n");
        return sb.toString();
    }

    // ─── Respuesta HTTP ──────────────────────────────────────────────────────

    public static String construirRespuestaHTTP(String htmlContent) {
        try {
            byte[] bytes = htmlContent.getBytes("UTF-8");
            return "HTTP/1.1 200 OK\r\n"
                 + "Content-Type: text/html; charset=UTF-8\r\n"
                 + "Content-Length: " + bytes.length + "\r\n"
                 + "Connection: close\r\n\r\n"
                 + htmlContent;
        } catch (Exception e) {
            return "HTTP/1.1 500 Internal Server Error\r\nContent-Type: text/plain\r\n\r\nError: " + e.getMessage();
        }
    }
}
