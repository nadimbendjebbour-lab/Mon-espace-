"""
Générateur Excel – Indice de Gini par déciles (xlsxwriter)
"""
import xlsxwriter

wb = xlsxwriter.Workbook("/home/user/Mon-espace-/TP_Indice_Gini_Deciles.xlsx")
ws = wb.add_worksheet("Indice de Gini")

# ── Formats ───────────────────────────────────────────────────────────────────
def fmt(d):
    return wb.add_format(d)

# Couleurs
BLK, WHT = "#000000", "#FFFFFF"
YEL  = "#FFFF00"
GRN  = "#92D050"
LBLU = "#BDD7EE"
DBL  = "#1F3F6D"
ORG  = "#F4B942"
LGR  = "#D9D9D9"
SAL  = "#FFCCCC"
RED  = "#C00000"
DRD  = "#C00000"

base = {"font_name": "Aptos Narrow", "font_size": 11, "valign": "vcenter"}

# En-têtes colonnes
hdr = {**base, "bold": True, "font_size": 10, "bg_color": DBL,
       "font_color": WHT, "align": "center", "border": 1, "text_wrap": True}
# Cellule saisie (jaune)
inp = {**base, "bg_color": YEL, "align": "center", "border": 1}
# Fréquence cumulée (bleu clair)
cum = {**base, "bg_color": LBLU, "align": "center", "border": 1,
       "num_format": "0.00\"%\""}
# Lorenz (orange)
lrz = {**base, "bg_color": ORG, "align": "center", "border": 1,
       "num_format": "0.00\"%\""}
# Fréquence fixe
frq = {**base, "align": "center", "border": 1, "num_format": "0.00\"%\""}
# Décile
dec = {**base, "align": "center", "border": 1}
# Totaux
tot = {**base, "bold": True, "bg_color": LGR, "align": "center",
       "border": 1, "num_format": "# ##0.00"}
tot_val = {**tot, "num_format": "# ##0.00"}

# Résultats droite
lbl_r = {**base, "bold": True, "font_size": 10, "bg_color": DBL,
         "font_color": WHT, "align": "right", "border": 1}
val_r = {**base, "bold": True, "font_size": 11, "bg_color": LBLU,
         "align": "center", "border": 1, "num_format": "# ##0.00"}
val_gini = {**base, "bold": True, "font_size": 14, "bg_color": SAL,
            "font_color": DRD, "align": "center", "border": 1,
            "num_format": "0.0000"}
val_aire = {**base, "bold": True, "font_size": 11, "bg_color": LBLU,
            "align": "center", "border": 1, "num_format": "0.0000"}
val_seuil = {**base, "bold": True, "font_size": 12, "bg_color": SAL,
             "font_color": RED, "align": "center", "border": 1,
             "num_format": "# ##0.00"}
inp_pct = {**base, "bold": True, "font_size": 12, "bg_color": YEL,
           "align": "center", "border": 1, "num_format": "0\"%\""}
pct_unit = {**base, "bold": True, "bg_color": GRN, "align": "center",
            "border": 1}

# Titre bloc résultats
ttl_r = {**base, "bold": True, "font_size": 12, "bg_color": BLK,
         "font_color": WHT, "align": "center", "border": 1}
# Titre principal
ttl_main = {**base, "bold": True, "font_size": 13, "bg_color": BLK,
            "font_color": WHT, "align": "center", "valign": "vcenter"}
# Instructions
instr = {**base, "font_size": 10, "italic": True, "bg_color": GRN,
         "align": "left", "valign": "vcenter"}
# Note
note = {**base, "font_size": 9, "italic": True, "align": "left",
        "valign": "vcenter", "text_wrap": True}
# Signature
sign = {**base, "bold": True, "font_size": 11, "italic": True,
        "bg_color": BLK, "font_color": WHT, "align": "right",
        "valign": "vcenter"}
# Interprétation
interp = {**base, "font_size": 9, "italic": True, "bg_color": LGR,
          "align": "left", "valign": "vcenter", "text_wrap": True,
          "border": 1}

# ── Dimensions ────────────────────────────────────────────────────────────────
ws.set_column("A:A", 16)
ws.set_column("B:B", 10)
ws.set_column("C:C", 14)
ws.set_column("D:D", 18)
ws.set_column("E:E", 22)
ws.set_column("F:G", 2)
ws.set_column("H:I", 22)
ws.set_column("J:K", 14)

ws.set_row(0,  30)   # ligne 1
ws.set_row(1,  22)   # ligne 2
ws.set_row(2,  22)   # ligne 3
ws.set_row(3,  36)   # ligne 4 (en-têtes)
for r in range(4, 20):
    ws.set_row(r, 18)
ws.set_row(10, 8)    # séparateur
ws.set_row(14, 20)   # totaux
ws.set_row(16, 28)   # note 1
ws.set_row(17, 28)   # note 2
ws.set_row(19, 24)   # signature

# ── Titre principal (E1:K1) ──────────────────────────────────────────────────
ws.merge_range("E1:K1",
    "Calcul de l'indice de Gini par la formule des Trapèzes",
    fmt(ttl_main))

ws.merge_range("E2:K2",
    "  \u279c  Saisir les valeurs par déciles dans la partie jaune – "
    "Colonne A (revenus moyens par décile, triés croissants).",
    fmt(instr))

ws.merge_range("E3:K3",
    "  \u279c  Saisir le pourcentage conventionnel du seuil de pauvreté "
    "(cellule jaune J9 – défaut : 60 %).",
    fmt(instr))

# ── En-têtes colonnes (ligne 4 = index 3) ────────────────────────────────────
ws.write(3, 0, "Valeurs\n(revenu moyen)",            fmt(hdr))
ws.write(3, 1, "N° Décile",                          fmt(hdr))
ws.write(3, 2, "Fréquence\n(%)",                     fmt(hdr))
ws.write(3, 3, "Fréquence Cumulée\nCroissante (%)",  fmt(hdr))
ws.write(3, 4, "Part Cumulée\ndes Revenus – Lorenz (%)", fmt(hdr))

# ── Données exemples (déciles 1-10) ──────────────────────────────────────────
example = [520, 870, 1150, 1450, 1780, 2200, 2750, 3400, 4350, 7200]

for i, val in enumerate(example):
    r = 4 + i        # lignes 5-14 (index 4-13)

    # Col A : valeur saisie (jaune)
    ws.write(r, 0, val, fmt(inp))

    # Col B : n° décile
    ws.write(r, 1, i + 1, fmt(dec))

    # Col C : fréquence fixe 10 %
    ws.write(r, 2, 10, fmt(frq))

    # Col D : fréquence cumulée croissante
    if i == 0:
        ws.write_formula(r, 3, "=C5", fmt(cum))
    else:
        ws.write_formula(r, 3, f"=D{r}+C{r+1}", fmt(cum))

    # Col E : part cumulée des revenus (Lorenz)
    ws.write_formula(r, 4,
        f"=SUM($A$5:A{r+1})/SUM($A$5:$A$14)*100",
        fmt(lrz))

# ── Ligne totaux (ligne 15, index 14) ────────────────────────────────────────
ws.write(14, 0, "=SUM(A5:A14)",  fmt(tot_val))
ws.write(14, 2, "=SUM(C5:C14)", fmt(tot))
ws.write(14, 3, "=D14",          fmt(tot))
ws.write(14, 4, "=E14",          fmt(tot))

# ── Bloc résultats (colonnes H-K) ────────────────────────────────────────────
# Titre
ws.merge_range("H5:K5", "RÉSULTATS AUTOMATIQUES", fmt(ttl_r))

# ① Revenu total
ws.merge_range("H6:I6", "\u2460 Revenu Total :", fmt(lbl_r))
ws.merge_range("J6:K6", "=SUM(A5:A14)",          fmt(val_r))

# ② Revenu moyen
ws.merge_range("H7:I7", "\u2461 Revenu Moyen :", fmt(lbl_r))
ws.merge_range("J7:K7", "=SUM(A5:A14)/10",       fmt(val_r))

# ③ Revenu médian
ws.merge_range("H8:I8", "\u2462 Revenu Médian :", fmt(lbl_r))
ws.merge_range("J8:K8", "=(A9+A10)/2",            fmt(val_r))

# ④ % seuil de pauvreté (saisie jaune)
ws.merge_range("H9:I9",
    "\u2463 % seuil de pauvreté :", fmt(lbl_r))
ws.write("J9", 60, fmt(inp_pct))
ws.write("K9", "%", fmt(pct_unit))

# ⑤ Seuil de pauvreté
ws.merge_range("H10:I10", "\u2464 SEUIL de PAUVRETÉ :", fmt(lbl_r))
ws.merge_range("J10:K10", "=J8*J9/100",               fmt(val_seuil))

# (séparateur : ligne 11 = index 10, hauteur 8 déjà fixée)

# ⑥ Aire sous la courbe
ws.merge_range("H12:I12",
    "\u2465 Aire sous la courbe (trapèzes) :", fmt(lbl_r))
ws.merge_range("J12:K12",
    "=0.05*(2*SUM(E5:E13)/100+E14/100)", fmt(val_aire))

# ⑦ Indice de Gini
ws.merge_range("H13:I13", "\u2466 INDICE de GINI :", fmt(lbl_r))
ws.merge_range("J13:K13", "=1-2*J12",               fmt(val_gini))

# Interprétation
ws.merge_range("H14:K15",
    "Gini = 0  \u2192  Égalité parfaite\n"
    "Gini = 1  \u2192  Inégalité maximale\n"
    "Gini > 0,35  \u2192  Forte inégalité",
    fmt(interp))

# ── Notes méthodologiques (lignes 17-18, index 16-17) ─────────────────────────
ws.merge_range("A17:E17",
    "Méthode : un point de la courbe de Lorenz a pour abscisse n/10 "
    "(n = numéro de décile, de 0 à 10) et pour ordonnée la part "
    "cumulée des revenus correspondante.", fmt(note))

ws.merge_range("A18:E18",
    "Formule des trapèzes : Aire ≈ (h/2) × [y\u2080 + 2y\u2081 + … "
    "+ 2y\u2089 + y\u2081\u2080]  avec h = 0,1 ;  y\u2080 = 0 ;  "
    "y\u2081…y\u2081\u2080 = colonne E ;  Gini = 1 \u2212 2 \u00d7 Aire",
    fmt(note))

# ── Signature (ligne 20, index 19) ───────────────────────────────────────────
ws.merge_range("A20:E20", "Réalisé par : Traore", fmt(sign))

# ── Graphique – Courbe de Concentration ──────────────────────────────────────
chart = wb.add_chart({"type": "line"})
chart.set_title({"name": "Courbe de Concentration"})
chart.set_x_axis({
    "name": "Fréquence cumulée de la population (%)",
    "min": 0, "max": 100,
    "major_gridlines": {"visible": True,
                        "line": {"color": "#D9D9D9", "dash_type": "solid"}},
})
chart.set_y_axis({
    "name": "Part cumulée des revenus (%)",
    "min": 0, "max": 100,
    "major_gridlines": {"visible": True,
                        "line": {"color": "#D9D9D9", "dash_type": "solid"}},
})
chart.set_legend({"position": "bottom"})
chart.set_size({"width": 480, "height": 300})

# Série Lorenz (courbe de concentration)
chart.add_series({
    "name":       "Courbe de Lorenz",
    "categories": ["Indice de Gini", 4, 3, 13, 3],   # D5:D14
    "values":     ["Indice de Gini", 4, 4, 13, 4],   # E5:E14
    "line":       {"color": "#0070C0", "width": 2.25},
    "marker":     {"type": "circle", "size": 4,
                   "border": {"color": "#0070C0"},
                   "fill":   {"color": "#0070C0"}},
})

# Diagonale (égalité parfaite) – points 0,0 et 100,100
# On utilise une petite table en col M (index 12) hors vue
ws.write(4,  12, 0)
ws.write(14, 12, 100)
ws.write(4,  13, 0)
ws.write(14, 13, 100)

chart.add_series({
    "name":       "Égalité parfaite",
    "categories": ["Indice de Gini", 4, 12, 14, 12],   # M5,M15
    "values":     ["Indice de Gini", 4, 13, 14, 13],   # N5,N15
    "line":       {"color": "#FF0000", "width": 1.5,
                   "dash_type": "dash"},
})

ws.insert_chart("H17", chart, {"x_offset": 0, "y_offset": 5})

# ── Figer les volets ──────────────────────────────────────────────────────────
ws.freeze_panes(4, 0)

wb.close()
print("✅  Fichier généré : /home/user/Mon-espace-/TP_Indice_Gini_Deciles.xlsx")
