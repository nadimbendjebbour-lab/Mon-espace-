# ============================================================
# SAE - Préparation des données athlétisme - GR16
# Nadim, Sekou, Maleine, Houssame
# Encadrants : Imad Hamri & Lea Winston
# ============================================================

# 1 - Environnement de travail
setwd("Z:/R/sae")
install.packages("tidyverse")
install.packages("arrow")
install.packages("lubridate")
library(tidyverse)
library(lubridate)
library(arrow)

# 2 - Import des données
dataF <- read_parquet("donnee_femme.parquet")
load("donnee_homme.rdata")

# OPTIMISATION 1 : Étapes 3+4 fusionnées
# L'ajout du Sexe se fait directement dans bind_rows(),
# sans avoir à réassigner dataF et donnee_homme séparément
data_all <- bind_rows(
  dataF        %>% mutate(Sexe = "F"),
  donnee_homme %>% mutate(Sexe = "H")
)

# OPTIMISATION 2 : Étapes 5 à 14 regroupées en un seul pipeline
# Dans dplyr, un mutate() évalue les colonnes dans l'ordre :
# on peut donc utiliser une colonne créée plus haut dans le même mutate()
# Ex : Mois_compet créé → utilisé pour Saison dans le même mutate()
# Ex : Age_FFA créé → utilisé pour Categorie_age dans le même mutate()
# Ex : Type_epreuve créé → utilisé pour Performance_propre dans le même mutate()
data_all <- data_all %>%

  # OPTIMISATION 2a : Étapes 5+6+7+8 fusionnées en un seul mutate()
  mutate(
    Perf        = as.numeric(gsub(",", ".", as.character(Perf))),
    Date_naiss  = ymd(Date_naiss),
    Date_compet = ymd(Date_compet),
    Annee_naiss = year(Date_naiss),
    Mois_compet = month(Date_compet),
    Saison      = ifelse(Mois_compet >= 9, year(Date_compet) + 1, year(Date_compet)),
    Age_relatif = time_length(interval(Date_naiss, Date_compet), "years")
  ) %>%

  # Étape 9 - Suppression des âges incohérents (inchangée, chainée)
  filter(Age_relatif >= 0 & Age_relatif <= 100) %>%

  # OPTIMISATION 2b : Étapes 10+11+12+13 fusionnées en un seul mutate()
  mutate(
    # Trimestre de naissance (factor ordonné)
    Trimestre_naiss = factor(
      quarter(Date_naiss),
      levels  = c(1, 2, 3, 4),
      labels  = c("Q1", "Q2", "Q3", "Q4"),
      ordered = TRUE
    ),
    # Catégorie d'âge FFA — Age_FFA est créé et utilisé dans le même mutate()
    Age_FFA = Saison - Annee_naiss,
    Categorie_age = factor(
      case_when(
        Age_FFA <= 6                ~ "Baby Athle",
        Age_FFA %in% c(7, 8, 9)    ~ "Eveil Athletique",
        Age_FFA %in% c(10, 11)     ~ "U12 / Poussins",
        Age_FFA %in% c(12, 13)     ~ "U14 / Benjamins",
        Age_FFA %in% c(14, 15)     ~ "U16 / Minimes",
        Age_FFA %in% c(16, 17)     ~ "U18 / Cadets",
        Age_FFA %in% c(18, 19)     ~ "U20 / Juniors",
        Age_FFA %in% c(20, 21, 22) ~ "U23 / Espoirs",
        Age_FFA %in% 23:31         ~ "Seniors",
        Age_FFA >= 32              ~ "Masters"
      ),
      levels  = c("Baby Athle", "Eveil Athletique", "U12 / Poussins",
                  "U14 / Benjamins", "U16 / Minimes", "U18 / Cadets",
                  "U20 / Juniors", "U23 / Espoirs", "Seniors", "Masters"),
      ordered = TRUE
    ),
    # Type d'épreuve — créé et utilisé pour Performance_propre dans le même mutate()
    Type_epreuve = case_when(
      Discipline %in% c("disque", "javelot", "marteau",
                        "longueur", "triple saut",
                        "hauteur", "perche", "poids") ~ "Distance",
      TRUE ~ "Temps"
    ),
    Performance_propre = case_when(
      Type_epreuve == "Distance" ~ abs(Perf) / 100,
      Type_epreuve == "Temps"    ~ Perf / 100,
      TRUE                       ~ NA_real_
    )
  ) %>%

  # OPTIMISATION 2c : Étape 14 — les 2 filter() fusionnés en 1 seul
  filter(
    !is.na(Performance_propre) &
    ((Type_epreuve == "Distance" & Performance_propre > 0) |
     (Type_epreuve == "Temps"    & Performance_propre < 0))
  )

# 15 - Suppression des valeurs aberrantes par méthode IQR
# Cette étape reste séparée : le group_by impose une structure distincte
n_avant <- nrow(data_all)

data_all <- data_all %>%
  group_by(Discipline, Categorie_age) %>%
  mutate(
    Q1_perf   = quantile(Performance_propre, 0.25, na.rm = TRUE),
    Q3_perf   = quantile(Performance_propre, 0.75, na.rm = TRUE),
    IQR_perf  = Q3_perf - Q1_perf,
    borne_inf = Q1_perf - 1.5 * IQR_perf,
    borne_sup = Q3_perf + 1.5 * IQR_perf
  ) %>%
  filter(Performance_propre >= borne_inf & Performance_propre <= borne_sup) %>%
  select(-Q1_perf, -Q3_perf, -IQR_perf, -borne_inf, -borne_sup) %>%
  ungroup()

n_apres <- nrow(data_all)
cat("Lignes supprimées :", n_avant - n_apres, "\n")
cat("Pourcentage supprimé :", round((n_avant - n_apres) / n_avant * 100, 2), "%\n")

# 16 - Vérifications finales
dim(data_all)
names(data_all)
summary(data_all$Performance_propre)
table(data_all$Type_epreuve)
table(data_all$Categorie_age)
table(data_all$Trimestre_naiss)
colSums(is.na(data_all))

# 17 - Graphique : répartition par trimestre de naissance
effectif_trim <- data_all %>%
  group_by(Trimestre_naiss) %>%
  summarise(nb = n()) %>%
  ungroup()

ggplot(effectif_trim) +
  aes(x = Trimestre_naiss, y = nb, fill = Trimestre_naiss) +
  geom_bar(stat = "identity") +
  scale_fill_manual(values = c(
    "Q1" = "#225533",
    "Q2" = "#2E6B44",
    "Q3" = "#3D8A57",
    "Q4" = "#56A86E"
  )) +
  labs(
    title = "Répartition des athlètes par trimestre de naissance",
    x     = "Trimestre de naissance",
    y     = "Nombre d'observations"
  ) +
  theme_minimal()

# 18 - Sauvegarde de la base finale
saveRDS(data_all, "data_all_clean_final.rds")
