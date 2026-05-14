package agrocomparador.ui;

import agrocomparador.business.ProductoService;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.*;
import java.util.stream.Collectors;

public class HTMLBuilder {

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
            String error, String filtroAplicado, String accion, String filtroFechaDesde, String filtroFechaHasta) {
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

        html.append(construirStatsGrid());
        html.append(construirToolbar(filtroAplicado, filtroFechaDesde, filtroFechaHasta));

        if (error != null && !error.isEmpty())
            html.append("<div class='alert alert-error'><span>⚠️</span> ").append(escapeHTML(error)).append("</div>\n");

        if (productos != null && !productos.isEmpty()) {
            html.append("<p class='results-info'>").append(productos.size()).append(" registros encontrados</p>\n");
            html.append(construirGrafica(productos));
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
            String error, String filtroAplicado, String accion) {
        return construirRespuestaHTML(productos, error, filtroAplicado, accion, null, null);
    }

    public static String construirRespuestaHTML(List<Map<String, Object>> productos,
            String error, String filtroAplicado) {
        return construirRespuestaHTML(productos, error, filtroAplicado, null, null, null);
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
            + "@media(max-width:720px) { .stats-grid { grid-template-columns:repeat(2,1fr); } .header-sources { display:none; } .sep { display:none; } }\n"
            + "@media(max-width:500px) { .stats-grid { grid-template-columns:1fr 1fr; } }\n"
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
        return "";
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

    private static String construirToolbar(String filtro, String filtroFechaDesde, String filtroFechaHasta) {
        List<String> fechas = new ArrayList<>();
        try { fechas = ProductoService.obtenerFechasDisponibles(); } catch (Exception ignored) {}

        // fechas ordenadas DESC: primero el más reciente, último el más antiguo
        String maxFecha = fechas.isEmpty() ? "" : fechas.get(0);
        String minFecha = fechas.isEmpty() ? "" : fechas.get(fechas.size() - 1);

        StringBuilder tb = new StringBuilder();
        tb.append("<div class='toolbar'>\n");
        tb.append("  <form method='GET' action='/' style='display:flex;gap:8px;flex:1;min-width:0;align-items:center;flex-wrap:wrap;'>\n");

        tb.append("    <div class='search-wrap'>");
        tb.append("<input type='text' name='producto' placeholder='Buscar por producto, variedad, fuente u origen...' ");
        if (filtro != null && !filtro.isEmpty())
            tb.append("value='").append(escapeHTML(filtro)).append("'");
        tb.append("/></div>\n");

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

                    t.append("      <tr class='group-child ").append(rowCls).append("' data-group='").append(gid).append("' data-subgroup='").append(sgid).append("'>\n");
                    t.append("        <td class='child-indent'>└</td>\n");
                    t.append("        <td></td>\n");
                    t.append("        <td>").append(escapeHTML(p.getOrDefault("fuente", "").toString())).append("</td>\n");
                    t.append("        <td class='precio'>€").append(String.format(Locale.US, "%.2f", precio)).append("</td>\n");
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
            DayOfWeek dow = dia.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY)
                diasHabiles.add(dia.toString());
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
            DayOfWeek dow = dia.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY)
                diasHabiles.add(dia.toString());
            dia = dia.minusDays(1);
        }
        Set<String> cargadas = new HashSet<>(fechasConDatos);
        String hoy = LocalDate.now().toString();

        StringBuilder sb = new StringBuilder();
        sb.append("<div id='modal-vaciar' class='modal-overlay' onclick=\"if(event.target===this)this.style.display='none'\">\n");
        sb.append("  <div class='modal-box'>\n");
        sb.append("    <div class='modal-title'>🗑️ Vaciar datos del scraper</div>\n");
        sb.append("    <div class='modal-sub'>Elige qué datos eliminar de la base de datos:</div>\n");
        sb.append("    <div style='display:flex;flex-direction:column;gap:7px;max-height:310px;overflow-y:auto;padding-right:4px;margin-bottom:14px;'>\n");

        for (String fechaDia : diasHabiles) {
            boolean tieneDatos = cargadas.contains(fechaDia);
            String fmt = formatearFechaCorta(fechaDia);
            String sufijo = fechaDia.equals(hoy) ? " — hoy" : "";
            String clsExtra = tieneDatos ? " dia-vaciar" : " dia-sin-datos";

            sb.append("      <label class='dia-option").append(clsExtra).append("'>\n");
            sb.append("        <div class='dia-label'><span>📅 ")
              .append(escapeHTML(fmt)).append(escapeHTML(sufijo)).append("</span></div>\n");
            if (tieneDatos) {
                sb.append("        <a href='/vaciar?fecha=").append(fechaDia)
                  .append("' class='btn btn-danger' style='padding:4px 12px;font-size:12px;' ")
                  .append("onclick=\"return confirm('¿Vaciar datos del ").append(escapeHTML(fmt)).append("?')\">")
                  .append("🗑️ Vaciar</a>\n");
            } else {
                sb.append("        <span class='dia-badge dia-badge-proc' style='background:#F5F5F5;color:#BBBBBB;'>sin datos</span>\n");
            }
            sb.append("      </label>\n");
        }

        sb.append("    </div>\n");
        sb.append("    <div style='display:flex;gap:8px;justify-content:space-between;align-items:center;border-top:1px solid #EEF2F7;padding-top:14px;'>\n");
        sb.append("      <a href='/vaciar' class='btn btn-danger' onclick=\"return confirm('¿Vaciar TODOS los datos del scraper?')\">🗑️ Vaciar todo</a>\n");
        sb.append("      <button type='button' onclick=\"document.getElementById('modal-vaciar').style.display='none'\" class='btn btn-ghost'>Cerrar</button>\n");
        sb.append("    </div>\n");
        sb.append("  </div>\n");
        sb.append("</div>\n");
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
