"""
Générateur du tableau Excel - Calcul de l'indice de Gini par déciles
"""
import openpyxl
from openpyxl.styles import PatternFill, Font, Alignment, Border, Side
from openpyxl.utils import get_column_letter
from openpyxl.chart import LineChart, Reference
from openpyxl.chart.series import SeriesLabel

wb = openpyxl.Workbook()
ws = wb.active
ws.title = "Indice de Gini"

# ── couleurs ──────────────────────────────────────────────────────────────────
C_BLACK   = "00000000"
C_WHITE   = "00FFFFFF"
C_YELLOW  = "00FFFF00"
C_GREEN   = "0092D050"
C_LGREY   = "00D9D9D9"
C_RED     = "00FF0000"
C_SALMON  = "00FFCCCC"
C_ORANGE  = "00FFCC99"
C_DBLUE   = "001F3F6D"
C_LBLUE   = "00BDD7EE"
C_ORANGE2 = "00F4B942"

def mk_fill(hex_color):
    return PatternFill(start_color=hex_color, end_color=hex_color, fill_type="solid")

def mk_border(style="thin"):
    s = Side(style=style)
    return Border(left=s, right=s, top=s, bottom=s)

def mk_font(bold=False, size=11, color=C_BLACK, italic=False):
    return Font(bold=bold, size=size, color=color, italic=italic)

def mk_align(h="center", v="center", wrap=False):
    return Alignment(horizontal=h, vertical=v, wrap_text=wrap)

def style(cell, fill=None, font=None, align=None, border=None):
    if fill:   cell.fill   = fill
    if font:   cell.font   = font
    if align:  cell.alignment = align
    if border: cell.border  = border

# ── largeurs de colonnes ──────────────────────────────────────────────────────
ws.column_dimensions['A'].width = 16
ws.column_dimensions['B'].width = 10
ws.column_dimensions['C'].width = 14
ws.column_dimensions['D'].width = 18
ws.column_dimensions['E'].width = 22
ws.column_dimensions['F'].width = 2   # séparateur visuel
ws.column_dimensions['G'].width = 2
ws.column_dimensions['H'].width = 14
ws.column_dimensions['I'].width = 4
ws.column_dimensions['J'].width = 18
ws.column_dimensions['K'].width = 18
ws.column_dimensions['L'].width = 18
ws.column_dimensions['M'].width = 14

# hauteurs de lignes
for r in range(1, 40):
    ws.row_dimensions[r].height = 18
ws.row_dimensions[1].height = 30
ws.row_dimensions[2].height = 22
ws.row_dimensions[3].height = 22
ws.row_dimensions[13].height = 22  # ligne totaux

# ============================================================
# BLOC TITRE (E1:M3)
# ============================================================
ws.merge_cells('E1:M1')
ws['E1'] = "Calcul de l'indice de Gini par la formule des Trapèzes"
style(ws['E1'],
      fill=mk_fill(C_BLACK),
      font=mk_font(bold=True, size=13, color=C_WHITE),
      align=mk_align(h="center"))

ws.merge_cells('E2:M2')
ws['E2'] = "  ➜  Saisir les valeurs par déciles dans la partie jaune – Colonne A (revenus moyens par décile, triés croissants)."
style(ws['E2'],
      fill=mk_fill(C_GREEN),
      font=mk_font(size=10, italic=True),
      align=mk_align(h="left"))

ws.merge_cells('E3:M3')
ws['E3'] = "  ➜  Saisir le pourcentage conventionnel du seuil de pauvreté (cellule jaune J27 – défaut : 60 %)."
style(ws['E3'],
      fill=mk_fill(C_GREEN),
      font=mk_font(size=10, italic=True),
      align=mk_align(h="left"))

# ============================================================
# EN-TÊTES DE COLONNES (ligne 4)
# ============================================================
headers = [
    (1, "Valeurs\n(revenu moyen)"),
    (2, "N° Décile"),
    (3, "Fréquence\n(%)"),
    (4, "Fréquence Cumulée\nCroissante (%)"),
    (5, "Part Cumulée\ndes Revenus – Lorenz (%)"),
]
for col, label in headers:
    c = ws.cell(row=4, column=col, value=label)
    style(c,
          fill=mk_fill(C_DBLUE),
          font=mk_font(bold=True, size=10, color=C_WHITE),
          align=mk_align(h="center", wrap=True),
          border=mk_border())
ws.row_dimensions[4].height = 36

# ============================================================
# DONNÉES : lignes 5 à 14  (déciles 1 à 10)
# ============================================================
# Valeurs d'exemple (remplaçables par l'utilisateur)
example = [520, 870, 1150, 1450, 1780, 2200, 2750, 3400, 4350, 7200]

for i, val in enumerate(example, start=1):
    row = 4 + i   # row 5..14

    # --- Colonne A : valeurs (saisie, fond jaune) ---
    a = ws.cell(row=row, column=1, value=val)
    style(a,
          fill=mk_fill(C_YELLOW),
          font=mk_font(size=11),
          align=mk_align(),
          border=mk_border())

    # --- Colonne B : n° décile ---
    b = ws.cell(row=row, column=2, value=i)
    style(b,
          font=mk_font(size=11),
          align=mk_align(),
          border=mk_border())

    # --- Colonne C : fréquence (chaque décile = 10 %) ---
    c = ws.cell(row=row, column=3, value=10)
    style(c,
          font=mk_font(size=11),
          align=mk_align(),
          border=mk_border())
    c.number_format = '0.00"%"'

    # --- Colonne D : fréquence cumulée croissante ---
    if i == 1:
        d_formula = "=C5"
    else:
        d_formula = f"=D{row-1}+C{row}"
    d = ws.cell(row=row, column=4, value=d_formula)
    style(d,
          fill=mk_fill(C_LBLUE),
          font=mk_font(size=11),
          align=mk_align(),
          border=mk_border())
    d.number_format = '0.00"%"'

    # --- Colonne E : part cumulée des revenus (Lorenz) ---
    if i == 1:
        e_formula = "=SOMME($A$5:A5)/SOMME($A$5:$A$14)*100"
    else:
        e_formula = f"=SOMME($A$5:A{row})/SOMME($A$5:$A$14)*100"
    e = ws.cell(row=row, column=5, value=e_formula)
    style(e,
          fill=mk_fill(C_ORANGE2),
          font=mk_font(size=11),
          align=mk_align(),
          border=mk_border())
    e.number_format = '0.00"%"'

# ============================================================
# LIGNE TOTAUX / VÉRIFICATION (ligne 15)
# ============================================================
ws.row_dimensions[15].height = 20
totals = [
    (1, "=SOMME(A5:A14)",   "Revenu Total",     C_LGREY),
    (3, "=SOMME(C5:C14)",   "= 100 %",          C_LGREY),
    (4, "=D14",             "= 100 %",          C_LGREY),
    (5, "=E14",             "= 100 %",          C_LGREY),
]
ws.cell(row=15, column=1, value="← TOTAUX").font = mk_font(bold=True)
for col, formula, tip, fill_color in totals:
    c = ws.cell(row=15, column=col, value=formula)
    style(c,
          fill=mk_fill(fill_color),
          font=mk_font(bold=True, size=11),
          align=mk_align(),
          border=mk_border())
    c.number_format = '# ##0.00'

# ============================================================
# BLOC CALCULS RÉSULTATS (colonne H-M, lignes 6-30)
# ============================================================

def result_row(ws, row, label, formula, label_fill, value_fill, number_format='# ##0.00'):
    """Pose un label (H) et une valeur calculée (J:K)."""
    lc = ws.cell(row=row, column=8, value=label)
    style(lc,
          fill=mk_fill(label_fill),
          font=mk_font(bold=True, size=10),
          align=mk_align(h="right"),
          border=mk_border())
    ws.merge_cells(start_row=row, start_column=8, end_row=row, end_column=9)

    vc = ws.cell(row=row, column=10, value=formula)
    style(vc,
          fill=mk_fill(value_fill),
          font=mk_font(bold=True, size=11),
          align=mk_align(),
          border=mk_border())
    ws.merge_cells(start_row=row, start_column=10, end_row=row, end_column=11)
    vc.number_format = number_format
    return vc

# ── Titre bloc résultats ──────────────────────────────────────────────────────
ws.merge_cells('H5:K5')
ws['H5'] = "RÉSULTATS AUTOMATIQUES"
style(ws['H5'],
      fill=mk_fill(C_BLACK),
      font=mk_font(bold=True, size=12, color=C_WHITE),
      align=mk_align(h="center"))

# ── 1. Revenu total ───────────────────────────────────────────────────────────
result_row(ws, 6, "① Revenu Total :", "=SOMME(A5:A14)", C_DBLUE, C_LBLUE)

# ── 2. Revenu moyen ──────────────────────────────────────────────────────────
result_row(ws, 7, "② Revenu Moyen :", "=SOMME(A5:A14)/10", C_DBLUE, C_LBLUE)

# ── 3. Revenu médian ─────────────────────────────────────────────────────────
ws['H8'] = "③ Revenu Médian (interpolé) :"
style(ws['H8'],
      fill=mk_fill(C_DBLUE),
      font=mk_font(bold=True, size=10, color=C_WHITE),
      align=mk_align(h="right"),
      border=mk_border())
ws.merge_cells('H8:I8')
# Interpolation : D5 = 50 %, donc entre décile 5 et 6 → médian = (A9+A10)/2
median_cell = ws.cell(row=8, column=10, value="=(A9+A10)/2")
style(median_cell,
      fill=mk_fill(C_LBLUE),
      font=mk_font(bold=True, size=11),
      align=mk_align(),
      border=mk_border())
ws.merge_cells('J8:K8')
median_cell.number_format = '# ##0.00'

# ── 4. % seuil de pauvreté (cellule saisie) ──────────────────────────────────
ws['H9'] = "④ % conventionnel du seuil de pauvreté :"
style(ws['H9'],
      fill=mk_fill(C_DBLUE),
      font=mk_font(bold=True, size=10, color=C_WHITE),
      align=mk_align(h="right"),
      border=mk_border())
ws.merge_cells('H9:I9')

pct_cell = ws.cell(row=9, column=10, value=60)
style(pct_cell,
      fill=mk_fill(C_YELLOW),
      font=mk_font(bold=True, size=12),
      align=mk_align(),
      border=mk_border())
pct_cell.number_format = '0 "%"'

unit_cell = ws.cell(row=9, column=11, value="%")
style(unit_cell,
      fill=mk_fill(C_GREEN),
      font=mk_font(bold=True, size=11),
      align=mk_align(),
      border=mk_border())

# ── 5. Seuil de pauvreté ─────────────────────────────────────────────────────
# Cellule J9 = % (ex: 60), J8 = médian  →  seuil = médian × J9/100
seuil_cell = result_row(ws, 10, "⑤ SEUIL de PAUVRETÉ :", "=J8*J9/100",
                        C_RED, C_SALMON)
seuil_cell.font = mk_font(bold=True, size=12, color=C_RED)

# ── 6. Aire sous la courbe de Lorenz (trapèzes) ───────────────────────────────
# Formule : A = 0,05 × (2×SOMME(E5:E13)/100 + E14/100)
# = (1/2) × (1/10) × [y0 + 2×y1 + ... + 2×y9 + y10]  avec y0=0, y10=1
aire_formula = "=0.05*(2*SOMME(E5:E13)/100+E14/100)"
aire_cell = result_row(ws, 12, "⑥ Aire sous la courbe (trapèzes) :", aire_formula,
                       C_DBLUE, C_LBLUE, '0.0000')

# ── 7. Indice de Gini ─────────────────────────────────────────────────────────
gini_formula = "=1-2*J12"
gini_cell = result_row(ws, 13, "⑦ INDICE de GINI :", gini_formula,
                       "00C00000", C_SALMON, '0.0000')
gini_cell.font = mk_font(bold=True, size=14, color="00C00000")

# ── Ligne vide séparatrice ────────────────────────────────────────────────────
ws.row_dimensions[11].height = 8

# ── Interprétation ───────────────────────────────────────────────────────────
ws.merge_cells('H14:K15')
ws['H14'] = ('Gini = 0 → Égalité parfaite\n'
             'Gini = 1 → Inégalité maximale\n'
             'Gini > 0,35 → Forte inégalité')
style(ws['H14'],
      fill=mk_fill(C_LGREY),
      font=mk_font(size=9, italic=True),
      align=mk_align(h="left", wrap=True))

# ============================================================
# NOTE MÉTHODOLOGIQUE (en bas, ligne 18+)
# ============================================================
ws.merge_cells('A17:E17')
ws['A17'] = ("Méthode : un point de la courbe de Lorenz a pour abscisse n/10 "
             "(n = numéro de décile, de 0 à 10) et pour ordonnée la part cumulée des revenus correspondante.")
style(ws['A17'],
      font=mk_font(size=9, italic=True),
      align=mk_align(h="left", wrap=True))
ws.row_dimensions[17].height = 28

ws.merge_cells('A18:E18')
ws['A18'] = ("Formule des trapèzes : Aire ≈ (h/2) × [y₀ + 2y₁ + … + 2y₉ + y₁₀]  "
             "avec h = 0,1 ;  y₀ = 0 ;  y₁…y₁₀ = colonne E (Lorenz) ;  Gini = 1 − 2 × Aire")
style(ws['A18'],
      font=mk_font(size=9, italic=True),
      align=mk_align(h="left", wrap=True))
ws.row_dimensions[18].height = 28

# ============================================================
# GRAPHIQUE – Courbe de Lorenz + droite d'équirépartition
# ============================================================

# Données pour le graphique :
# x-axis  : D5:D14 (fréquences cumulées pop. 10..100)
# Lorenz  : E5:E14
# Équirép : on ajoute une série linéaire 10..100 dans une zone helper

# Zone helper pour la diagonale (col M, lignes 5-14) : 10,20,...,100
for i, row in enumerate(range(5, 15), start=1):
    ws.cell(row=row, column=13, value=i * 10)  # col M = 13

chart = LineChart()
chart.title = "Courbe de Lorenz"
chart.style = 10
chart.y_axis.title = "Part cumulée des revenus (%)"
chart.x_axis.title = "Fréquence cumulée de la population (%)"
chart.y_axis.numFmt = '0'
chart.x_axis.numFmt = '0'
chart.y_axis.scaling.min = 0
chart.y_axis.scaling.max = 100
chart.height = 14
chart.width  = 22

# Série Lorenz
lorenz_data = Reference(ws, min_col=5, min_row=4, max_row=14)  # avec titre
lorenz_cats = Reference(ws, min_col=4, min_row=5, max_row=14)
chart.add_data(lorenz_data, titles_from_data=True)
chart.set_categories(lorenz_cats)
chart.series[0].graphicalProperties.line.solidFill  = "0000B0F0"
chart.series[0].graphicalProperties.line.width = 20000

# Série diagonale (égalité parfaite)
diag_data = Reference(ws, min_col=13, min_row=5, max_row=14)
chart.add_data(diag_data)
chart.series[1].title = SeriesLabel(v="Égalité parfaite")
chart.series[1].graphicalProperties.line.solidFill = "00FF0000"
chart.series[1].graphicalProperties.line.width = 15000
chart.series[1].graphicalProperties.line.dashDot = "dash"

ws.add_chart(chart, "H17")

# ============================================================
# FREEZE et zoom
# ============================================================
ws.freeze_panes = "A5"
ws.sheet_view.zoomScale = 90

# ============================================================
# SAUVEGARDE
# ============================================================
output_path = "/home/user/Mon-espace-/TP_Indice_Gini_Deciles.xlsx"
wb.save(output_path)
print(f"✅  Fichier généré : {output_path}")
