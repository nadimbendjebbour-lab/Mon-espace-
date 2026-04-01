package com.garage;

import com.healthmarketscience.jackcess.*;
import java.io.File;
import java.time.LocalDate;
import java.util.*;

public class CreateGarageDB {

    public static void main(String[] args) throws Exception {
        String outputPath = args.length > 0 ? args[0] : "GARAGE_2026.accdb";
        File dbFile = new File(outputPath);
        if (dbFile.exists()) dbFile.delete();

        System.out.println("Creation de la base : " + dbFile.getAbsolutePath());

        try (Database db = new DatabaseBuilder(dbFile)
                .setFileFormat(Database.FileFormat.V2016)
                .create()) {

            // ── Tables principales ──────────────────────────────────────
            Table client    = createClientTable(db);
            Table vehicule  = createVehiculeTable(db);
            Table ouvrier   = createOuvrierTable(db);
            Table operation = createOperationTable(db);
            Table visite    = createVisiteTable(db);
            Table demander  = createDemanderTable(db);
            Table realiser  = createRealiserTable(db);

            // ── Tables de sauvegarde (copies) ───────────────────────────
            Table savClient    = createSaveTable(db, "C Save CLIENT",
                    client.getColumns());
            Table savVehicule  = createSaveTable(db, "C Save VEHICULE",
                    vehicule.getColumns());
            Table savOuvrier   = createSaveTable(db, "C Save OUVRIER",
                    ouvrier.getColumns());
            Table savOperation = createSaveTable(db, "C Save OPERATION",
                    operation.getColumns());
            Table savVisite    = createSaveTable(db, "C Save VISITE",
                    visite.getColumns());
            Table savDemander  = createSaveTable(db, "C Save DEMANDER",
                    demander.getColumns());
            Table savRealiser  = createSaveTable(db, "C Save REALISER",
                    realiser.getColumns());

            // ── Données ─────────────────────────────────────────────────
            insertClients(client, savClient);
            insertVehicules(vehicule, savVehicule);
            insertOuvriers(ouvrier, savOuvrier);
            insertOperations(operation, savOperation);
            insertVisites(visite, savVisite);
            insertDemander(demander, savDemander);
            insertRealiser(realiser, savRealiser);

            System.out.println("Base GARAGE_2026.accdb creee avec succes !");
            System.out.println("Tables creees : CLIENT, VEHICULE, OUVRIER, OPERATION,");
            System.out.println("               VISITE, DEMANDER, REALISER");
            System.out.println("               + 7 tables C Save XXX");
        }
    }

    // ═══════════════════════════════════════════════════════════════════
    // CREATION DES TABLES
    // ═══════════════════════════════════════════════════════════════════

    static Table createClientTable(Database db) throws Exception {
        return new TableBuilder("CLIENT")
            .addColumn(new ColumnBuilder("NumClient",  DataType.LONG))
            .addColumn(new ColumnBuilder("Nom",        DataType.TEXT).setLength(50))
            .addColumn(new ColumnBuilder("Prenom",     DataType.TEXT).setLength(50))
            .addColumn(new ColumnBuilder("Adresse",    DataType.TEXT).setLength(100))
            .addColumn(new ColumnBuilder("CP",         DataType.TEXT).setLength(10))
            .addColumn(new ColumnBuilder("Ville",      DataType.TEXT).setLength(50))
            .addColumn(new ColumnBuilder("Tel",        DataType.TEXT).setLength(20))
            .toTable(db);
    }

    static Table createVehiculeTable(Database db) throws Exception {
        return new TableBuilder("VEHICULE")
            .addColumn(new ColumnBuilder("Immatriculation", DataType.TEXT).setLength(20))
            .addColumn(new ColumnBuilder("Marque",          DataType.TEXT).setLength(50))
            .addColumn(new ColumnBuilder("Modele",          DataType.TEXT).setLength(50))
            .addColumn(new ColumnBuilder("NumClient",       DataType.LONG))
            .toTable(db);
    }

    static Table createOuvrierTable(Database db) throws Exception {
        return new TableBuilder("OUVRIER")
            .addColumn(new ColumnBuilder("NumOuvrier",    DataType.LONG))
            .addColumn(new ColumnBuilder("NomOuvrier",    DataType.TEXT).setLength(50))
            .addColumn(new ColumnBuilder("PrenomOuvrier", DataType.TEXT).setLength(50))
            .toTable(db);
    }

    static Table createOperationTable(Database db) throws Exception {
        return new TableBuilder("OPERATION")
            .addColumn(new ColumnBuilder("CodeOpe",     DataType.TEXT).setLength(10))
            .addColumn(new ColumnBuilder("LibOpe",      DataType.TEXT).setLength(100))
            .addColumn(new ColumnBuilder("CoutHoraire", DataType.DOUBLE))
            .toTable(db);
    }

    static Table createVisiteTable(Database db) throws Exception {
        return new TableBuilder("VISITE")
            .addColumn(new ColumnBuilder("NumVisite",      DataType.LONG))
            .addColumn(new ColumnBuilder("DateVisite",     DataType.SHORT_DATE_TIME))
            .addColumn(new ColumnBuilder("Immatriculation",DataType.TEXT).setLength(20))
            .addColumn(new ColumnBuilder("KM",             DataType.LONG))
            .toTable(db);
    }

    static Table createDemanderTable(Database db) throws Exception {
        return new TableBuilder("DEMANDER")
            .addColumn(new ColumnBuilder("NumVisite", DataType.LONG))
            .addColumn(new ColumnBuilder("CodeOpe",   DataType.TEXT).setLength(10))
            .toTable(db);
    }

    static Table createRealiserTable(Database db) throws Exception {
        return new TableBuilder("REALISER")
            .addColumn(new ColumnBuilder("NumVisite",  DataType.LONG))
            .addColumn(new ColumnBuilder("CodeOpe",    DataType.TEXT).setLength(10))
            .addColumn(new ColumnBuilder("NumOuvrier", DataType.LONG))
            .addColumn(new ColumnBuilder("Duree",      DataType.DOUBLE))
            .toTable(db);
    }

    /** Crée une table de sauvegarde avec les mêmes colonnes qu'une table source. */
    static Table createSaveTable(Database db, String name,
                                  List<? extends Column> cols) throws Exception {
        TableBuilder tb = new TableBuilder(name);
        for (Column c : cols) {
            ColumnBuilder cb = new ColumnBuilder(c.getName(), c.getType());
            if (c.getType() == DataType.TEXT) cb.setLength(c.getLength());
            tb.addColumn(cb);
        }
        return tb.toTable(db);
    }

    // ═══════════════════════════════════════════════════════════════════
    // INSERTION DES DONNEES
    // ═══════════════════════════════════════════════════════════════════

    static void insertClients(Table t, Table save) throws Exception {
        Object[][] data = {
            {1, "DUPONT",   "Jean",    "12 rue de la Paix",       "75001", "Paris",     "0612345678"},
            {2, "MARTIN",   "Marie",   "5 avenue Foch",           "69001", "Lyon",      "0698765432"},
            {3, "BERNARD",  "Pierre",  "8 boulevard Victor Hugo", "13001", "Marseille", "0611223344"},
            {4, "LEROY",    "Sophie",  "23 rue des Lilas",        "75002", "Paris",     "0755443322"},
            {5, "MOREAU",   "Paul",    "15 rue du Commerce",      "31000", "Toulouse",  "0644332211"},
            {6, "SIMON",    "Claire",  "3 impasse des Roses",     "33000", "Bordeaux",  "0677889900"},
            {7, "LAURENT",  "Nicolas", "47 avenue Jean Jaures",   "59000", "Lille",     "0623456789"},
            {8, "THOMAS",   "Emma",    "9 rue du Moulin",         "67000", "Strasbourg","0688990011"},
        };
        for (Object[] row : data) {
            t.addRow(row);
            save.addRow(row);
        }
    }

    static void insertVehicules(Table t, Table save) throws Exception {
        Object[][] data = {
            {"AB-123-CD", "Peugeot",    "308",   1},
            {"EF-456-GH", "Renault",    "Clio",  2},
            {"IJ-789-KL", "Citroën",    "C3",    3},
            {"MN-012-OP", "Volkswagen", "Golf",  4},
            {"QR-345-ST", "Toyota",     "Yaris", 5},
            {"UV-678-WX", "Peugeot",    "208",   1},  // Client 1 a 2 véhicules
            {"YZ-901-AB", "Ford",       "Focus", 6},
            {"CD-234-EF", "Renault",    "Megane",7},
            {"GH-567-IJ", "BMW",        "Serie3",8},
            {"KL-890-MN", "Citroën",    "C5",    3},  // Client 3 a 2 véhicules
        };
        for (Object[] row : data) {
            t.addRow(row);
            save.addRow(row);
        }
    }

    static void insertOuvriers(Table t, Table save) throws Exception {
        Object[][] data = {
            {1, "GARCIA",   "Miguel"},
            {2, "PETIT",    "Thomas"},
            {3, "ROUSSEAU", "Antoine"},
            {4, "LAMBERT",  "Lucas"},
        };
        for (Object[] row : data) {
            t.addRow(row);
            save.addRow(row);
        }
    }

    static void insertOperations(Table t, Table save) throws Exception {
        Object[][] data = {
            {"OPE01", "Vidange",                     80.0},
            {"OPE02", "Revision complete",            90.0},
            {"OPE03", "Changement plaquettes frein",  70.0},
            {"OPE04", "Changement pneus",             60.0},
            {"OPE05", "Diagnostic electronique",     100.0},
            {"OPE06", "Remplacement batterie",        75.0},
            {"OPE07", "Climatisation",                85.0},
            {"OPE08", "Remplacement courroie",        95.0},
        };
        for (Object[] row : data) {
            t.addRow(row);
            save.addRow(row);
        }
    }

    static void insertVisites(Table t, Table save) throws Exception {
        // 32 visites - les visites 23 et 32 sont configurées pour tester la facture
        // Plusieurs visites par véhicule pour tester les doublons (Req 4-6-1)
        Object[][] data = {
            { 1, LocalDate.of(2024,  1,  8), "AB-123-CD", 32000},
            { 2, LocalDate.of(2024,  1, 15), "EF-456-GH", 18500},
            { 3, LocalDate.of(2024,  1, 22), "IJ-789-KL", 55000},
            { 4, LocalDate.of(2024,  2,  5), "MN-012-OP", 27000},
            { 5, LocalDate.of(2024,  2, 12), "QR-345-ST", 41000},
            { 6, LocalDate.of(2024,  2, 19), "UV-678-WX", 12000},
            { 7, LocalDate.of(2024,  3,  4), "YZ-901-AB", 63000},
            { 8, LocalDate.of(2024,  3, 11), "AB-123-CD", 33500}, // doublon AB-123-CD
            { 9, LocalDate.of(2024,  3, 18), "CD-234-EF", 22000},
            {10, LocalDate.of(2024,  4,  2), "GH-567-IJ", 48000},
            {11, LocalDate.of(2024,  4,  9), "KL-890-MN", 71000},
            {12, LocalDate.of(2024,  4, 16), "EF-456-GH", 19800}, // doublon EF-456-GH
            {13, LocalDate.of(2024,  5,  7), "MN-012-OP", 28500}, // doublon MN-012-OP
            {14, LocalDate.of(2024,  5, 14), "QR-345-ST", 43000}, // doublon QR-345-ST
            {15, LocalDate.of(2024,  5, 21), "AB-123-CD", 35000}, // doublon AB-123-CD
            {16, LocalDate.of(2024,  6,  3), "IJ-789-KL", 57000}, // doublon IJ-789-KL
            {17, LocalDate.of(2024,  6, 10), "UV-678-WX", 14000}, // doublon UV-678-WX
            {18, LocalDate.of(2024,  6, 17), "YZ-901-AB", 65000}, // doublon YZ-901-AB
            {19, LocalDate.of(2024,  7,  1), "CD-234-EF", 24000}, // doublon CD-234-EF
            {20, LocalDate.of(2024,  7,  8), "GH-567-IJ", 50000}, // doublon GH-567-IJ
            {21, LocalDate.of(2024,  7, 22), "KL-890-MN", 73000}, // doublon KL-890-MN
            {22, LocalDate.of(2024,  8,  5), "MN-012-OP", 30000}, // doublon MN-012-OP
            // ── Visite 23 : pour tester la facture TTC ──────────────────
            {23, LocalDate.of(2024,  8, 19), "AB-123-CD", 36500}, // doublon AB-123-CD
            {24, LocalDate.of(2024,  9,  2), "EF-456-GH", 21000}, // doublon EF-456-GH
            {25, LocalDate.of(2024,  9,  9), "QR-345-ST", 44500}, // doublon QR-345-ST
            {26, LocalDate.of(2024,  9, 16), "UV-678-WX", 16000}, // doublon UV-678-WX
            {27, LocalDate.of(2024, 10,  7), "YZ-901-AB", 67000}, // doublon YZ-901-AB
            {28, LocalDate.of(2024, 10, 14), "IJ-789-KL", 59000}, // doublon IJ-789-KL
            {29, LocalDate.of(2024, 10, 21), "GH-567-IJ", 52000}, // doublon GH-567-IJ
            {30, LocalDate.of(2024, 11,  4), "CD-234-EF", 26000}, // doublon CD-234-EF
            {31, LocalDate.of(2024, 11, 18), "KL-890-MN", 75000}, // doublon KL-890-MN
            // ── Visite 32 : pour tester la facture TTC ──────────────────
            {32, LocalDate.of(2024, 12,  2), "EF-456-GH", 22500}, // doublon EF-456-GH
            {33, LocalDate.of(2024, 12,  9), "AB-123-CD", 38000}, // doublon AB-123-CD
            {34, LocalDate.of(2024, 12, 16), "MN-012-OP", 31500}, // doublon MN-012-OP
            {35, LocalDate.of(2025,  1,  6), "QR-345-ST", 46000}, // doublon QR-345-ST
        };
        for (Object[] row : data) {
            t.addRow(row);
            save.addRow(row);
        }
    }

    static void insertDemander(Table t, Table save) throws Exception {
        // Opérations demandées par visite
        Object[][] data = {
            // Visite 1
            { 1, "OPE01"}, { 1, "OPE02"},
            // Visite 2
            { 2, "OPE03"}, { 2, "OPE04"},
            // Visite 3
            { 3, "OPE05"},
            // Visite 4
            { 4, "OPE01"}, { 4, "OPE06"},
            // Visite 5
            { 5, "OPE02"}, { 5, "OPE07"},
            // Visite 6
            { 6, "OPE08"},
            // Visite 7
            { 7, "OPE01"},
            // Visite 8
            { 8, "OPE03"}, { 8, "OPE05"},
            // Visite 9
            { 9, "OPE02"},
            // Visite 10
            {10, "OPE04"}, {10, "OPE06"},
            // Visite 11
            {11, "OPE01"},
            // Visite 12
            {12, "OPE07"}, {12, "OPE08"},
            // Visite 13
            {13, "OPE01"}, {13, "OPE02"},
            // Visite 14
            {14, "OPE03"},
            // Visite 15
            {15, "OPE05"}, {15, "OPE06"},
            // Visite 16
            {16, "OPE01"},
            // Visite 17
            {17, "OPE04"},
            // Visite 18
            {18, "OPE02"}, {18, "OPE07"},
            // Visite 19
            {19, "OPE08"},
            // Visite 20
            {20, "OPE01"}, {20, "OPE03"},
            // Visite 21
            {21, "OPE05"},
            // Visite 22
            {22, "OPE06"},
            // Visite 23 - Vidange + Révision complète
            {23, "OPE01"}, {23, "OPE02"},
            // Visite 24
            {24, "OPE04"},
            // Visite 25
            {25, "OPE07"},
            // Visite 26
            {26, "OPE01"}, {26, "OPE08"},
            // Visite 27
            {27, "OPE02"},
            // Visite 28
            {28, "OPE03"},
            // Visite 29
            {29, "OPE05"}, {29, "OPE06"},
            // Visite 30
            {30, "OPE01"},
            // Visite 31
            {31, "OPE04"},
            // Visite 32 - Frein + Diagnostic
            {32, "OPE03"}, {32, "OPE05"},
            // Visite 33
            {33, "OPE07"},
            // Visite 34
            {34, "OPE01"}, {34, "OPE02"},
            // Visite 35
            {35, "OPE08"},
        };
        for (Object[] row : data) {
            t.addRow(row);
            save.addRow(row);
        }
    }

    static void insertRealiser(Table t, Table save) throws Exception {
        // Opérations réalisées (NumVisite, CodeOpe, NumOuvrier, Duree en 1/4h)
        // Visite 23 :
        //   OPE01 Vidange     : 4 x 1/4h = 1h  x 80€  = 80€  HT  => 96€  TTC
        //   OPE02 Révision    : 8 x 1/4h = 2h  x 90€  = 180€ HT  => 216€ TTC
        //   TOTAL visite 23   :                  260€ HT => 312€ TTC
        // Visite 32 :
        //   OPE03 Freins      : 6 x 1/4h = 1.5h x 70€ = 105€ HT  => 126€ TTC
        //   OPE05 Diagnostic  : 2 x 1/4h = 0.5h x100€ =  50€ HT  =>  60€ TTC
        //   TOTAL visite 32   :                  155€ HT => 186€ TTC
        Object[][] data = {
            { 1, "OPE01", 1, 4.0}, { 1, "OPE02", 2, 8.0},
            { 2, "OPE03", 1, 6.0}, { 2, "OPE04", 3, 8.0},
            { 3, "OPE05", 2, 2.0},
            { 4, "OPE01", 1, 4.0}, { 4, "OPE06", 4, 3.0},
            { 5, "OPE02", 2,10.0}, { 5, "OPE07", 3, 6.0},
            { 6, "OPE08", 1,12.0},
            { 7, "OPE01", 2, 4.0},
            { 8, "OPE03", 3, 6.0}, { 8, "OPE05", 1, 2.0},
            { 9, "OPE02", 2, 8.0},
            {10, "OPE04", 4, 8.0}, {10, "OPE06", 1, 3.0},
            {11, "OPE01", 3, 4.0},
            {12, "OPE07", 2, 6.0}, {12, "OPE08", 4,12.0},
            {13, "OPE01", 1, 4.0}, {13, "OPE02", 3, 8.0},
            {14, "OPE03", 2, 6.0},
            {15, "OPE05", 1, 2.0}, {15, "OPE06", 4, 3.0},
            {16, "OPE01", 3, 4.0},
            {17, "OPE04", 2, 8.0},
            {18, "OPE02", 1,10.0}, {18, "OPE07", 4, 6.0},
            {19, "OPE08", 3,12.0},
            {20, "OPE01", 2, 4.0}, {20, "OPE03", 1, 6.0},
            {21, "OPE05", 4, 2.0},
            {22, "OPE06", 3, 3.0},
            // ── Visite 23 (test facture) ──
            {23, "OPE01", 1, 4.0},
            {23, "OPE02", 2, 8.0},
            {24, "OPE04", 3, 8.0},
            {25, "OPE07", 1, 6.0},
            {26, "OPE01", 2, 4.0}, {26, "OPE08", 4,12.0},
            {27, "OPE02", 3, 8.0},
            {28, "OPE03", 1, 6.0},
            {29, "OPE05", 2, 2.0}, {29, "OPE06", 4, 3.0},
            {30, "OPE01", 3, 4.0},
            {31, "OPE04", 1, 8.0},
            // ── Visite 32 (test facture) ──
            {32, "OPE03", 1, 6.0},
            {32, "OPE05", 3, 2.0},
            {33, "OPE07", 2, 6.0},
            {34, "OPE01", 4, 4.0}, {34, "OPE02", 1, 8.0},
            {35, "OPE08", 3,12.0},
        };
        for (Object[] row : data) {
            t.addRow(row);
            save.addRow(row);
        }
    }
}
