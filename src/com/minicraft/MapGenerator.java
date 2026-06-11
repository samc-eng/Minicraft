package com.minicraft;

public class MapGenerator {

    // =========================================================================
    // CONSTANTES : IDs du Sol (floor[][]) — doivent correspondre à ItemRegistry
    // =========================================================================
    public static final int FLOOR_GRASS = 1;   // Grass       (grass.png)
    public static final int FLOOR_STONE = 3;   // Gray Dirt   (gray_dirt.png) — sol de grotte
    public static final int FLOOR_WATER = 29;  // Water       (water.png)
    public static final int FLOOR_SAND  = 4;   // Sand        (sand.png)

    // CONSTANTES : IDs des Blocs (blocks[][])
    public static final int BLOCK_EMPTY         = 0;
    public static final int BLOCK_ROCK          = 5;   // Stone          (stone.png)
    public static final int BLOCK_TREE          = 25;  // Tree           (tree.png)
    public static final int BLOCK_CAVE_ENTRANCE = 23;  // Obsidian Door  (obsidian_door.png)
    public static final int BLOCK_OBSIDIAN      = 8;   // Obsidian       (obsidian.png)
    public static final int BLOCK_ORE_IRON      = 36;  // Iron Ore       (iron_ore.png)
    public static final int BLOCK_ORE_GOLD      = 37;  // Gold Ore       (gold_ore.png)
    public static final int BLOCK_ORE_GEM       = 38;  // Gem Ore        (gem_ore.png)
    public static final int BLOCK_ORE_COAL      = 47;  // Coal Ore       (coal_ore.png)

    // =========================================================================
    // PARAMÈTRES DE GÉNÉRATION — modifie ces valeurs pour ajuster le rendu
    // =========================================================================

    // "Zoom" du bruit : plus basse = îles plus grandes et massives.
    // Pour une carte 512x512, 0.004 est un bon point de départ.
    private static final double NOISE_SCALE = 0.004;

    // Seuil de terrain : une tuile passe de l'eau à la terre si (bruit - falloff) > valeur.
    // Augmenter → moins de terre. Diminuer → plus de terre.
    private static final double ISLAND_THRESHOLD = -0.15;

    // Seuil de sable : zone tampon juste au-dessus de l'eau → plage.
    // Doit être > ISLAND_THRESHOLD. Trop grand = plages immenses.
    private static final double SAND_THRESHOLD = 0.05;

    // Octaves du bruit fractal : plus = côtes plus détaillées, légèrement plus lent.
    private static final int    OCTAVES     = 6;

    // Persistance : poids des hautes fréquences. 0.5 = standard.
    private static final double PERSISTENCE = 0.5;

    // Lacunarité : multiplicateur de fréquence par octave. 2.0 = standard.
    private static final double LACUNARITY  = 2.0;

    // Gradient radial : force qui "enfonce" les bords en eau.
    // Augmenter → mer plus large autour des îles.
    private static final double FALLOFF_STRENGTH = 2.5;

    // --- Forêts ---
    // Échelle du bruit : 0.025 = patches de ~40 tuiles. Baisser = forêts plus grandes.
    private static final double FOREST_SCALE     = 0.025;
    // Seuil d'apparition des arbres. Baisser → plus de forêt.
    private static final double FOREST_THRESHOLD = 0.55;
    // Densité aléatoire au sein d'une zone forêt (0.0–1.0). Baisser → forêts plus clairsemées.
    private static final double FOREST_DENSITY   = 0.40;

    // --- Roches ---
    // Échelle plus élevée = clusters plus petits et épars.
    private static final double ROCK_SCALE       = 0.03;
    // Seuil plus haut → roches plus rares que les arbres.
    private static final double ROCK_THRESHOLD   = 0.62;
    // Densité aléatoire au sein d'une zone rocheuse.
    private static final double ROCK_DENSITY     = 0.85;

    // --- Grottes (Automates Cellulaires) ---
    // Ratio initial de murs (0.0–1.0). Plus élevé = grotte plus fermée.
    private static final double CAVE_WALL_RATIO     = 0.48;
    // Nombre d'itérations CA. Plus = formes plus lisses et arrondies.
    private static final int    CAVE_ITERATIONS     = 5;
    // Seuil : une cellule devient mur si elle a >= N voisins murs (sur 8).
    private static final int    CAVE_BIRTH_LIMIT    = 5;
    // Nombre d'entrées de grotte placées sur la surface.
    private static final int    CAVE_ENTRANCE_COUNT = 3;


    // =========================================================================
    // ÉTAPE 1 : Remplissage initial avec l'océan
    // =========================================================================

    /**
     * Remplit toute la grille de sol avec de l'eau.
     * Doit être appelée en premier.
     */
    public void fillWithOcean(int width, int height, int[][] floor) {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                floor[x][y] = FLOOR_WATER;
            }
        }
    }


    // =========================================================================
    // ÉTAPE 2 : Génération des îles (Simplex Noise fractal + gradient radial)
    // =========================================================================

    /**
     * Fait émerger la terre et les plages par-dessus l'océan.
     * Combine un bruit Simplex fractal (FBM) et un gradient radial qui force
     * les bords de la carte à rester en eau.
     *
     * @param seed Graine de génération — même seed = même carte.
     */
    public void generateIslands(int width, int height, int[][] floor, long seed) {
        SimplexNoise noise = new SimplexNoise(seed);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {

                // 1. Valeur de bruit fractal (entre ~-1.0 et ~1.0)
                double noiseValue = fbm(noise, x, y);

                // 2. Gradient radial : 0.0 au centre de la carte, 1.0 aux coins
                double falloff = computeFalloff(x, y, width, height);

                // 3. Soustraction : les bords sont "enfoncés" sous le seuil → restent en eau
                double finalValue = noiseValue - falloff;

                // 4. Affectation du biome selon la hauteur
                if (finalValue > SAND_THRESHOLD) {
                    floor[x][y] = FLOOR_GRASS;
                } else if (finalValue > ISLAND_THRESHOLD) {
                    floor[x][y] = FLOOR_SAND;  // plage : zone tampon entre eau et herbe
                }
                // Sinon → FLOOR_WATER déjà posé par fillWithOcean
            }
        }
    }

    // =========================================================================
    // ÉTAPE 3 : Génération des forêts
    // =========================================================================

    /**
     * Place des arbres en clusters organiques sur les tuiles d'herbe.
     * Règles : pas d'arbre sur le sable, pas d'arbre adjacent à l'eau ou au sable.
     *
     * @param seed Même seed que generateIslands pour la cohérence du monde.
     */
    public void generateForests(int width, int height, int[][] floor, int[][] blocks, long seed) {
        SimplexNoise noise = new SimplexNoise(seed + 1);
        java.util.Random rng = new java.util.Random(seed + 1);

        // On évite les bords (x=0, y=0) pour le check des voisins
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {

                // Seulement sur l'herbe
                if (floor[x][y] != FLOOR_GRASS) continue;

                // Pas d'arbre si un voisin (8 directions) est eau ou sable
                if (hasNeighbor(floor, x, y, FLOOR_WATER) || hasNeighbor(floor, x, y, FLOOR_SAND)) continue;

                // Bruit de forêt : valeur haute = zone forestière
                double forestValue = noise.eval(x * FOREST_SCALE, y * FOREST_SCALE);

                if (forestValue > FOREST_THRESHOLD && rng.nextDouble() < FOREST_DENSITY) {
                    blocks[x][y] = BLOCK_TREE;
                }
            }
        }
    }


    // =========================================================================
    // ÉTAPE 4 : Génération des roches
    // =========================================================================

    /**
     * Place des blocs de roche en clusters épars sur les tuiles d'herbe.
     * Les roches n'écrasent pas les arbres déjà posés.
     *
     * @param seed Même seed que generateIslands pour la cohérence du monde.
     */
    public void generateRocks(int width, int height, int[][] floor, int[][] blocks, long seed) {
        SimplexNoise noise = new SimplexNoise(seed + 2);
        java.util.Random rng = new java.util.Random(seed + 2);

        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {

                // Seulement sur l'herbe, et pas si un arbre est déjà là
                if (floor[x][y] != FLOOR_GRASS) continue;
                if (blocks[x][y] != BLOCK_EMPTY) continue;

                double rockValue = noise.eval(x * ROCK_SCALE, y * ROCK_SCALE);

                if (rockValue > ROCK_THRESHOLD && rng.nextDouble() < ROCK_DENSITY) {
                    blocks[x][y] = BLOCK_ROCK;
                }
            }
        }
    }


    // =========================================================================
    // ÉTAPE 5 : Entrées de grottes sur la surface
    // =========================================================================

    /**
     * Place CAVE_ENTRANCE_COUNT portes d'obsidienne sur des tuiles d'herbe,
     * loin de l'eau et du sable. Les positions sont stockées dans portals.
     */
    public void placeCaveEntrances(int width, int height, int[][] floor, int[][] blocks,
                                   java.util.List<int[]> portals, long seed) {
        java.util.Random rng = new java.util.Random(seed + 77);
        int placed = 0;
        int attempts = 0;

        while (placed < CAVE_ENTRANCE_COUNT && attempts < width * height / 4) {
            attempts++;
            int x = rng.nextInt(width - 4) + 2;
            int y = rng.nextInt(height - 4) + 2;

            if (floor[x][y] != FLOOR_GRASS)               continue;
            if (blocks[x][y] != BLOCK_EMPTY)               continue;
            if (hasNeighbor(floor, x, y, FLOOR_WATER))    continue;
            if (hasNeighbor(floor, x, y, FLOOR_SAND))     continue;

            blocks[x][y] = BLOCK_CAVE_ENTRANCE;
            portals.add(new int[]{x, y});

            // Décoration : cadre en obsidienne autour de la porte
            //   O O O
            //   O D O   (D = porte, O = obsidienne, . = libre devant)
            //   . . .
            int[][] frame = {
                {-1, -1}, {0, -1}, {1, -1},
                {-1,  0},          {1,  0}
            };
            for (int[] off : frame) {
                int fx = x + off[0];
                int fy = y + off[1];
                if (fx > 0 && fy > 0 && fx < width - 1 && fy < height - 1
                        && floor[fx][fy] == FLOOR_GRASS && blocks[fx][fy] == BLOCK_EMPTY) {
                    blocks[fx][fy] = BLOCK_OBSIDIAN;
                }
            }

            placed++;
        }
    }


    // =========================================================================
    // ÉTAPE 6 : Génération de grottes (Automates Cellulaires)
    // =========================================================================

    /**
     * Génère une grotte organique via l'algorithme d'automates cellulaires.
     * Résultat : sol de grotte partout, murs en BLOCK_ROCK, une porte de sortie
     * (BLOCK_CAVE_ENTRANCE) placée près du centre.
     *
     * @param depth Profondeur du niveau (1 = première grotte, 2 = plus profond…)
     */
    public void generateCave(int width, int height, int[][] floor, int[][] blocks,
                             java.util.List<int[]> portals, long seed, int depth) {
        java.util.Random rng = new java.util.Random(seed + depth * 31L);

        // --- Étape A : Initialisation aléatoire ---
        boolean[][] walls = new boolean[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                walls[x][y] = (x == 0 || y == 0 || x == width - 1 || y == height - 1)
                               || rng.nextDouble() < CAVE_WALL_RATIO;
            }
        }

        // --- Étape B : Itérations CA ---
        for (int iter = 0; iter < CAVE_ITERATIONS; iter++) {
            boolean[][] next = new boolean[width][height];
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
                        next[x][y] = true;
                        continue;
                    }
                    next[x][y] = countWallNeighbors(walls, x, y) >= CAVE_BIRTH_LIMIT;
                }
            }
            walls = next;
        }

        // --- Étape C : Conversion en floor/blocks ---
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                floor[x][y]  = FLOOR_STONE;
                blocks[x][y] = walls[x][y] ? BLOCK_ROCK : BLOCK_EMPTY;
            }
        }

        // --- Étape D : Porte de sortie près du centre ---
        int cx = width / 2;
        int cy = height / 2;
        outerLoop:
        for (int radius = 0; radius < Math.max(width, height); radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    int nx = cx + dx;
                    int ny = cy + dy;
                    if (nx > 1 && ny > 1 && nx < width - 2 && ny < height - 2
                            && blocks[nx][ny] == BLOCK_EMPTY) {
                        blocks[nx][ny] = BLOCK_CAVE_ENTRANCE;
                        portals.add(new int[]{nx, ny});
                        break outerLoop;
                    }
                }
            }
        }
    }

    private int countWallNeighbors(boolean[][] walls, int x, int y) {
        int count = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                int nx = x + dx;
                int ny = y + dy;
                if (nx < 0 || ny < 0 || nx >= walls.length || ny >= walls[0].length || walls[nx][ny])
                    count++;
            }
        }
        return count;
    }


    // =========================================================================
    // ÉTAPE 7 : Minerais dans les grottes
    // =========================================================================

    /**
     * Remplace certains murs (BLOCK_ROCK) par des minerais.
     * La rareté s'adapte à la profondeur : plus profond = plus d'or et de gemmes.
     *
     * @param depth Profondeur du niveau (1+).
     */
    public void placeOres(int width, int height, int[][] blocks, long seed, int depth) {
        java.util.Random rng = new java.util.Random(seed + depth * 53L);

        // Probabilités par profondeur (cumulées, testées dans l'ordre gem > or > fer > charbon)
        double gemChance  = 0.008 + (depth - 1) * 0.004;
        double goldChance = 0.025 + (depth - 1) * 0.008;
        double ironChance = 0.070 - (depth - 1) * 0.005;
        double coalChance = 0.090;  // charbon : commun et présent à toutes les profondeurs

        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if (blocks[x][y] != BLOCK_ROCK) continue;

                double roll = rng.nextDouble();
                if      (roll < gemChance)                                       blocks[x][y] = BLOCK_ORE_GEM;
                else if (roll < gemChance + goldChance)                          blocks[x][y] = BLOCK_ORE_GOLD;
                else if (roll < gemChance + goldChance + ironChance)             blocks[x][y] = BLOCK_ORE_IRON;
                else if (roll < gemChance + goldChance + ironChance + coalChance) blocks[x][y] = BLOCK_ORE_COAL;
            }
        }
    }


    // =========================================================================
    // UTILITAIRE : vérifie si un voisin direct (8 directions) a un ID donné
    // =========================================================================

    private boolean hasNeighbor(int[][] floor, int x, int y, int targetId) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                if (floor[x + dx][y + dy] == targetId) return true;
            }
        }
        return false;
    }


    /**
     * Bruit fractal (FBM) : superpose plusieurs octaves pour ajouter
     * du détail à différentes échelles (grandes formes + petits reliefs côtiers).
     */
    private double fbm(SimplexNoise noise, int x, int y) {
        double value     = 0.0;
        double amplitude = 1.0;
        double frequency = NOISE_SCALE;
        double maxValue  = 0.0;

        for (int i = 0; i < OCTAVES; i++) {
            value    += noise.eval(x * frequency, y * frequency) * amplitude;
            maxValue += amplitude;
            amplitude *= PERSISTENCE;
            frequency *= LACUNARITY;
        }

        return value / maxValue; // normalisation dans [-1, 1]
    }

    /**
     * Gradient radial sigmoïdal : produit 0.0 au centre et monte vers 1.0
     * aux bords, garantissant un océan périphérique quelle que soit la seed.
     * Distance de Chebyshev (max des axes) → gradient carré plutôt qu'ovale.
     */
    private double computeFalloff(int x, int y, int width, int height) {
        double nx = (2.0 * x / (width  - 1)) - 1.0;
        double ny = (2.0 * y / (height - 1)) - 1.0;

        double d  = Math.max(Math.abs(nx), Math.abs(ny));
        double a  = FALLOFF_STRENGTH;
        double da = Math.pow(d, a);

        return da / (da + Math.pow(1.0 - d, a));
    }


    // =========================================================================
    // CLASSE INTERNE : Simplex Noise 2D
    // Algorithme de Ken Perlin (2001), adapté par Stefan Gustavson.
    // Aucune dépendance externe — 100 % Java standard.
    // =========================================================================
    private static class SimplexNoise {

        private final int[] perm = new int[512];

        private static final int[][] GRAD3 = {
            { 1, 1, 0}, {-1, 1, 0}, { 1,-1, 0}, {-1,-1, 0},
            { 1, 0, 1}, {-1, 0, 1}, { 1, 0,-1}, {-1, 0,-1},
            { 0, 1, 1}, { 0,-1, 1}, { 0, 1,-1}, { 0,-1,-1}
        };

        SimplexNoise(long seed) {
            int[] p = new int[256];
            for (int i = 0; i < 256; i++) p[i] = i;

            // Mélange Fisher-Yates : reproductible via la seed
            java.util.Random rng = new java.util.Random(seed);
            for (int i = 255; i > 0; i--) {
                int j = rng.nextInt(i + 1);
                int t = p[i]; p[i] = p[j]; p[j] = t;
            }
            for (int i = 0; i < 512; i++) perm[i] = p[i & 255];
        }

        /** Retourne une valeur de bruit dans [-1, 1] pour le point (xin, yin). */
        double eval(double xin, double yin) {
            final double F2 = 0.5 * (Math.sqrt(3.0) - 1.0);
            final double G2 = (3.0 - Math.sqrt(3.0)) / 6.0;

            double s  = (xin + yin) * F2;
            int    i  = fastFloor(xin + s);
            int    j  = fastFloor(yin + s);
            double t  = (i + j) * G2;

            double x0 = xin - (i - t);
            double y0 = yin - (j - t);

            int i1 = (x0 > y0) ? 1 : 0;
            int j1 = (x0 > y0) ? 0 : 1;

            double x1 = x0 - i1 + G2;
            double y1 = y0 - j1 + G2;
            double x2 = x0 - 1.0 + 2.0 * G2;
            double y2 = y0 - 1.0 + 2.0 * G2;

            int gi0 = perm[(i      + perm[ j      & 255]) & 255] % 12;
            int gi1 = perm[(i + i1 + perm[(j + j1)& 255]) & 255] % 12;
            int gi2 = perm[(i +  1 + perm[(j +  1)& 255]) & 255] % 12;

            double n0 = kernel(gi0, x0, y0);
            double n1 = kernel(gi1, x1, y1);
            double n2 = kernel(gi2, x2, y2);

            return 70.0 * (n0 + n1 + n2);
        }

        private double kernel(int gi, double x, double y) {
            double t = 0.5 - x * x - y * y;
            if (t < 0) return 0.0;
            t *= t;
            return t * t * dot(GRAD3[gi], x, y);
        }

        private static double dot(int[] g, double x, double y) {
            return g[0] * x + g[1] * y;
        }

        private static int fastFloor(double x) {
            return x > 0 ? (int) x : (int) x - 1;
        }
    }
}
