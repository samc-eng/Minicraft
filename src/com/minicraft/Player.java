package com.minicraft;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyCode;

public class Player {
	private double x;
	private double y;
	private double vitesse;
	private int dir;
	private Image skin;
	private int anim=0;
	private boolean isMoved=false;
	private int attackTimer;
	private Inventory inventory = new Inventory();
	private int maxHealth = Config.PLAYER_MAX_HEALTH;
	private int health = maxHealth;
	private int energy = 10;
    private int energyTimer = 0;
    private int sprintTimer = 0;
    private int regenTimer = 0;
	private int invulnerabilityTimer = 0;
	private int shakeTimer = 0;                       // vibration de feedback quand une action échoue
	private static final int SHAKE_DURATION = 12;     // ~0.2s à 60 fps
	private int selectedSlot = 0;
	private ItemStack[] slot = new ItemStack[9];
	private boolean isSwimming = false;
    private int damageFlashTimer =0;
    private double respawnX;
    private double respawnY;
    private boolean isShiftPressed = false;

	public boolean up;
	public boolean down;
	public boolean right;
	public boolean left;
	
	public Player(double startX, double startY) {
		this.x=startX;
		this.y=startY;
        this.vitesse=Config.PLAYER_WALK_SPEED;
        this.invulnerabilityTimer=300;

        this.respawnX = this.x;
        this.respawnY = this.y;

		try {
			this.skin=new Image("file:resources/skins.png");
		} catch (Exception e){
			System.out.println("Erreur : impossible de charger le skin");
		}
		
	}
	
	public void tick(Level level, InputHandler input) {
		this.isMoved = false;
        this.isShiftPressed = input.isPressed(KeyCode.SHIFT);

        if (this.damageFlashTimer > 0) {
            this.damageFlashTimer--; 
        }
        if (this.invulnerabilityTimer > 0) {
            this.invulnerabilityTimer--; 
        }

        // 1. On détermine d'abord l'état actuel du joueur et sa récuparétion ou non d'néergie
        this.isSwimming = this.isInWater(level);
        boolean isSprinting = input.isPressed(KeyCode.SHIFT) && energy > 3;

        if (isSprinting) { 
            sprintTimer++;
            regenTimer = 0; // On arrête la régénération
            if (sprintTimer >= 60) {
                this.loseEnergy(1);
                sprintTimer = 0;
            }
        } else {
            sprintTimer = 0; // On arrête le sprint
            if (this.energy < 10) {

                regenTimer++;
                if (regenTimer >= 60) {
                    this.energy++;
                    regenTimer = 0;
                }
            }
        }

        // 2. On calcule la vitesse APPLIQUÉE (La seule fois où on touche à 'vitesse')
        if (this.isSwimming) {
            this.vitesse = isSprinting ? 1.2 * Config.PLAYER_WALK_SPEED : 0.5 * Config.PLAYER_WALK_SPEED;
        } else {
            this.vitesse = isSprinting ? Config.PLAYER_SPRINT_SPEED : Config.PLAYER_WALK_SPEED;
        }
        
        // 3. Maintenant on calcule le mouvement avec la bonne vitesse
        double futurX = x;
        double futurY = y;
        
        if (input.isPressed(KeyCode.Z)) { futurY -= vitesse; dir = 1; isMoved = true; }
        if (input.isPressed(KeyCode.S)) { futurY += vitesse; dir = 0; isMoved = true; }
        if (input.isPressed(KeyCode.D)) { futurX += vitesse; dir = 3; isMoved = true; }
        if (input.isPressed(KeyCode.Q)) { futurX -= vitesse; dir = 2; isMoved = true; }

		// touches pour choisir la case selectionnee dans la hotbar (1 a 9, ou F1 a F9)
		if (input.isClicked(KeyCode.DIGIT1) || input.isClicked(KeyCode.F1)) { setSelectedSlot(0); }
		if (input.isClicked(KeyCode.DIGIT2) || input.isClicked(KeyCode.F2)) { setSelectedSlot(1); }
		if (input.isClicked(KeyCode.DIGIT3) || input.isClicked(KeyCode.F3)) { setSelectedSlot(2); }
		if (input.isClicked(KeyCode.DIGIT4) || input.isClicked(KeyCode.F4)) { setSelectedSlot(3); }
		if (input.isClicked(KeyCode.DIGIT5) || input.isClicked(KeyCode.F5)) { setSelectedSlot(4); }
		if (input.isClicked(KeyCode.DIGIT6) || input.isClicked(KeyCode.F6)) { setSelectedSlot(5); }
		if (input.isClicked(KeyCode.DIGIT7) || input.isClicked(KeyCode.F7)) { setSelectedSlot(6); }
		if (input.isClicked(KeyCode.DIGIT8) || input.isClicked(KeyCode.F8)) { setSelectedSlot(7); }
		if (input.isClicked(KeyCode.DIGIT9) || input.isClicked(KeyCode.F9)) { setSelectedSlot(8); }

        // Régénération automatique
        // Si le joueur ne sprint pas, il récupère 1 point d'énergie toutes les ~1 secondes (60 ticks)
        if (this.energy < 10 && !isSprinting) { 
            energyTimer++;
            if (energyTimer >= 60) {
                this.energy++;
                energyTimer = 0; // On réinitialise le compteur
            }
        } else {
            energyTimer = 0; // Si on est au max, on reset le timer
        }
		
		boolean bloque= (level.isSolid(futurX+4, futurY+4) ||
				level.isSolid(futurX+Config.blockSize-4, futurY+4) ||
				level.isSolid(futurX+4, futurY+Config.blockSize) ||
				level.isSolid(futurX+Config.blockSize-4, futurY+Config.blockSize));
		
		if (! bloque) {
			x=futurX;
			y=futurY;
		}
		
		if (x < 0) {x = 0;}
		if (y < 0) {y = 0;}
		if (x > level.getWidth() * Config.blockSize - Config.blockSize) {x = level.getWidth() * Config.blockSize - Config.blockSize;}
		if (y > level.getHeight() * Config.blockSize - Config.blockSize) {y = level.getHeight() * Config.blockSize - Config.blockSize;};
		
		if (attackTimer>0) {attackTimer--;}
		if (invulnerabilityTimer > 0) invulnerabilityTimer--;
        if (damageFlashTimer > 0) damageFlashTimer--;
        if (shakeTimer > 0) shakeTimer--;
		
		if (isMoved) { 
			this.anim++;
		} else {
			this.anim=0;
		}
			
		if (input.isClicked(KeyCode.SPACE)) {
			this.interact(level,false);
		}
		
		if (input.isClicked(KeyCode.F)) {
			this.interact(level,true);
		}

		if (input.isClicked(KeyCode.H)) {
			this.dropSelectedItem(level);
		}

		
		for (Item item : level.getItems()) {
            double dx = (x + 6) - (item.getX() + 8);
            double dy = (y + 6) - (item.getY() + 8);
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance < 12) { 
                // On utilise notre nouvelle méthode unifiée
                this.pickUpItem(item.getStack());
                item.remove();        
            }
        }		
	}
	
	public void render(GraphicsContext gc) {
		//on crée les positions Blocks
		int xBlock=(int)((x+Config.blockSize/2)/Config.blockSize);
		int yBlock=(int)((y+Config.blockSize/2)/Config.blockSize);
		int cibleX= xBlock;
		int cibleY= yBlock;
		
		if (dir==0) {cibleY++;}
		if (dir==1) {cibleY--;}
		if (dir==3) {cibleX++;}
		if (dir==2) {cibleX--;}
		
        // on sélectionne Steve
        int skinRow = 8; 
        int skinCol;
        
        // dessin du perso + animation selon la direction
        boolean flip=false;        
        int mouv=this.anim/30%2;

        if (dir == 0) { // BAS
            skinCol = 0;
            if (isMoved && mouv == 1) flip = true;
        } 
        else if (dir == 1) { // HAUT
            skinCol = 1;
            if (isMoved && mouv == 1) flip = true; 
        }
        else if (dir == 2) { // GAUCHE
            // si on marche, on alterne entre Col 2 (Neutre) et Col 3 (Pas)
            skinCol = (isMoved && mouv == 1) ? 3 : 2; 
            flip = true; // Toujours miroir pour la gauche
        }
        else { // DROITE
            skinCol = (isMoved && mouv == 1) ? 3 : 2;
            flip = false;
        }
        
        int sourceX = skinCol * 15; 
        int sourceY = skinRow * 8; 
        
        
        // Décalage horizontal de vibration quand une action vient d'échouer.
        // sin() alterne gauche/droite rapidement pour donner l'effet "shake".
        double sx = x;
        if (shakeTimer > 0) {
            sx = x + Math.sin(shakeTimer * 1.6) * 2.5;
        }

        // on dessine l'animation
        if (this.isSwimming) {
            // --- DANS L'EAU : On ne prend que 8 pixels de haut (la moitié du skin) ---
            // On décale aussi le dessin vers le bas (+ Config.blockSize / 2) pour l'enfoncer
            if (flip) {
                gc.drawImage(this.skin,
                        sourceX, sourceY, 16, 8,
                        sx + Config.blockSize, y + (Config.blockSize / 2.0), -Config.blockSize, Config.blockSize / 2.0
                    );
            } else {
                gc.drawImage(this.skin,
                        sourceX, sourceY, 16, 8,
                        sx, y + (Config.blockSize / 2.0), Config.blockSize, Config.blockSize / 2.0
                    );
            }
        } else {
            // --- SUR TERRE : Dessin normal entier (16 pixels) ---
            if (flip) {
                gc.drawImage(this.skin,
                        sourceX, sourceY, 16, 16,
                        sx + Config.blockSize, y, -Config.blockSize, Config.blockSize
                    );
            } else {
                gc.drawImage(this.skin,
                        sourceX, sourceY, 16, 16,
                        sx, y, Config.blockSize, Config.blockSize
                    );
            }
        }

        // --- GESTION DES EFFETS VISUELS ---
        // 1. Flash rouge UNIQUEMENT quand on perd un coeur
        if (damageFlashTimer > 0) {
            gc.setFill(Color.rgb(255, 40, 40, 0.55));
            gc.fillRect(x, y, Config.blockSize, Config.blockSize);
        }

        // 2. Bouclier de spawn (Contour blanc clignotant doucement)
        if (isInvulnerable() && (invulnerabilityTimer / 15) % 2 == 0) {
            gc.setStroke(Color.rgb(255, 255, 255, 0.7)); 
            gc.setLineWidth(2); 
            gc.strokeRect(x, y, Config.blockSize, Config.blockSize); 
        }


		//on dessine un carré de sélection 
		gc.setStroke(Color.YELLOW);
		gc.setLineWidth(1);
		gc.strokeRect(cibleX * Config.blockSize, cibleY * Config.blockSize, Config.blockSize, Config.blockSize);
	
		
		if (attackTimer>0) {
			gc.setFill(Color.WHITE);
			if (dir == 0) gc.fillRect(x + 2, y + 14, 12, 4);
		    if (dir == 1) gc.fillRect(x + 2, y - 6, 12, 4);
		    if (dir == 2) gc.fillRect(x - 6, y + 2, 4, 12);
		    if (dir == 3) gc.fillRect(x + 14, y + 2, 4, 12);
		}
		
	}
	
    public boolean isInWater(Level level) {
        int idEau = 29;     

        // On calcule sur quelle case de la grille se trouve le joueur
        int gridX = (int) (this.getX() / Config.blockSize);
        int gridY = (int) (this.getY() / Config.blockSize);

        // On vérifie qu'on ne cherche pas en dehors de la carte
        if (gridX >= 0 && gridX < level.getWidth() && gridY >= 0 && gridY < level.getHeight()) {
            int[][] floor = level.getFloorArray();
            return floor[gridX][gridY] == idEau;
        }
        return false;
    }

	public void dropSelectedItem(Level level) {
		ItemStack stack = slot[selectedSlot];
		if (stack == null) return;

		// on calcule la case devant le joueur
		int xBlock = (int)((x + Config.blockSize/2) / Config.blockSize);
		int yBlock = (int)((y + Config.blockSize/2) / Config.blockSize);
		int cibleX = xBlock;
		int cibleY = yBlock;
		if (dir == 0) cibleY++;
		if (dir == 1) cibleY--;
		if (dir == 3) cibleX++;
		if (dir == 2) cibleX--;

		double dropX = cibleX * Config.blockSize + Config.blockSize / 2.0;
		double dropY = cibleY * Config.blockSize + Config.blockSize / 2.0;

		int id = stack.getItemId();
		level.dropItem(dropX, dropY, new ItemStack(id, 1));

		consumeSelectedItem();
		System.out.println("Item [" + id + "] jeté au sol.");
	}

	public void interact(Level level, boolean placeMode) {
		this.attackTimer=10;
		
		int xBlock=(int)((x+Config.blockSize/2)/Config.blockSize);
		int yBlock=(int)((y+Config.blockSize/2)/Config.blockSize);
		int cibleX= xBlock;
		int cibleY= yBlock;
		
		if (dir==0) {cibleY++;}
		if (dir==1) {cibleY--;}
		if (dir==3) {cibleX++;}
		if (dir==2) {cibleX--;}
		
		if (!placeMode) {
            int cibleBlock = level.getBlocks(cibleX * Config.blockSize, cibleY * Config.blockSize);

            // Outil actuellement tenu dans la hotbar (-1 si main nue)
            ItemStack held = getSelectedItem();
            int toolId = (held != null) ? held.getItemId() : -1;

            // --- Interaction avec le lit (inchangé) ---
            if (cibleBlock == 130 && !this.isShiftPressed) {
                this.respawnX = this.x;
                this.respawnY = this.y;
                System.out.println("Zzz... Point de réapparition sauvegardé ! (Maintiens SHIFT + Clic pour casser le lit)");
            }
            // --- DESTRUCTION D'UN BLOC ---
            else if (cibleBlock != 0) {
                // 1. A-t-on un outil assez puissant pour ce bloc ? (pierre/minerais)
                if (!Tools.canBreak(cibleBlock, toolId)) {
                    triggerShake(); // le bloc résiste : on fait vibrer le joueur
                    System.out.println("⛏️ Outil trop faible : il faut une meilleure pioche pour ce bloc !");
                } else {
                    // 2. Coût en énergie selon l'outil (bon outil = moins cher)
                    int cost = Tools.breakEnergyCost(cibleBlock, toolId);
                    if (this.energy >= cost) {
                        level.setBlocks(cibleX * Config.blockSize, cibleY * Config.blockSize, 0);
                        this.loseEnergy(cost);
                    } else {
                        triggerShake();
                        System.out.println("😮‍💨 Pas assez d'énergie ! (besoin de " + cost + ")");
                    }
                }
            }

            // Attaque des ennemis présents sur la case (gère sa propre énergie)
            attackEnemies(level, cibleX * Config.blockSize, cibleY * Config.blockSize);
        } else {
            // MODE CONSTRUCTION
			ItemStack stackInHand = getSelectedItem();

			if (stackInHand == null) {
				System.out.println("Aucun item sélectionné dans la hotbar.");
				return;
			}

            if (stackInHand.getItemId() == 113) { 
                // Empêcher de manger si on est déjà au max partout
                if (this.health >= this.maxHealth && this.energy >= 10) {
                    System.out.println("Tu es déjà en pleine forme ! Pas besoin de gaspiller de la nourriture.");
                    return;
                }

                // 1. Appliquer les effets du Steak
                this.heal(3);       // Donne 3 cœurs
                this.energy = 10;   // Rétablit toute l'énergie instantanément

                // 2. Consommer l'item (Ta méthode qui enlève 1 du slot et gère le null si 0)
                consumeSelectedItem();
                
                System.out.println("Miam ! Un bon steak. +3 coeurs et énergie restaurée !");
                return;
            }

			ItemDefinition def = stackInHand.getDefinition();
			if (def == null || !def.placeable) {
				System.out.println("Cet objet n'est pas un bloc posable.");
				return;
			}

			// on regarde si il y a deja quelque chose devant
			int existing = level.getBlocks(cibleX * Config.blockSize, cibleY * Config.blockSize);
			if (existing != 0) {
				System.out.println("Impossible de poser : la case est déjà occupée (bloc " + existing + ").");
				return;
			}

			// on pose le bloc et on retire 1 de la hotbar
			level.setBlocks(cibleX * Config.blockSize, cibleY * Config.blockSize, def.id);
			consumeSelectedItem();
			System.out.println("Bloc [" + def.name + "] posé !");
		}
	}
	
	public double getX() {return this.x;}
	public double getY() {return this.y;}
	public double getCenterX() {return this.x+Config.blockSize/2;}
	public double getCenterY() {return this.y+Config.blockSize/2;}
	public Inventory getInventory() {return this.inventory;}

	public void setX(double newX) {
		this.x = newX;
	}

	// Permet de modifier la position Y du joueur
	public void setY(double newY) {
		this.y = newY;
	}

	// Fait perdre de la vie au joueur
    public boolean takeDamage(int damage) {
        if (damage <= 0 || isInvulnerable() || this.health <= 0) {
            return false;
        }

        // --- ARMURE : chance d'ignorer complètement le coup ---
        double tankChance = armorSlots.getTankChance();
        if (tankChance > 0 && Math.random() < tankChance) {
            this.invulnerabilityTimer = 15; // court délai pour éviter le spam de coups
            System.out.println("🛡️ Armure ! Coup encaissé sans perdre de cœur ("
                    + (int)(tankChance * 100) + "% de tank).");
            return false;
        }

        // On applique les dégâts avec ta méthode sécurisée
        setHealth(this.health - damage);
        this.damageFlashTimer = 15;
        this.invulnerabilityTimer = 15;
        System.out.println("Aie ! Le joueur a pris " + damage + " degats. Vie restante : " + this.health);

        // --- NOUVEAU : On vérifie si le coup a été fatal ---
        if (this.health <= 0) {
            die();
        }

        return true;
    }

    private void die() {
        System.out.println("☠️ VOUS ÊTES MORT ! Réapparition au dernier point de sauvegarde...");
        
        // 1. Téléportation au lit (ou point de départ)
        this.x = this.respawnX;
        this.y = this.respawnY;
        
        // 2. Le personnage guérit
        setHealth(this.maxHealth); 
        this.energy = 10;

        // --- LE BOUCLIER DE SPAWN ---
        this.damageFlashTimer = 60; 
        this.invulnerabilityTimer = 60;
    }

    public void heal(int amount) {
        if (amount > 0) {
            setHealth(this.health + amount);
        }
    }

    public void setHealth(int health) {
        this.health = Math.max(0, Math.min(health, this.maxHealth));
    }

    public boolean isInvulnerable() {
        return this.invulnerabilityTimer > 0;
    }

    // appele quand le joueur recupere un item :
    // on essaye d'abord d'empiler dans la hotbar, sinon une case vide, sinon ca part dans l'inventaire
    public void pickUpItem(ItemStack stack) {
        int id     = stack.getItemId();
        int amount = stack.getAmount();

        // est-ce qu'on peut empiler avec une case existante ?
        for (int i = 0; i < 9; i++) {
            if (slot[i] != null && slot[i].getItemId() == id && !slot[i].isFull()) {
                slot[i].add(amount);
                return;
            }
        }
        // sinon premiere case vide
        for (int i = 0; i < 9; i++) {
            if (slot[i] == null) {
                slot[i] = new ItemStack(id, amount);
                return;
            }
        }
        // hotbar pleine, ca va dans l'inventaire
        this.inventory.add(stack);
    }

    // combien j'ai de cet item au total (hotbar + inventaire)
    public int amountOf(int id) {
        int total = this.inventory.getAmount(id);
        for (ItemStack s : slot) {
            if (s != null && s.getItemId() == id) total += s.getAmount();
        }
        return total;
    }

    public boolean has(int id, int count) {
        return amountOf(id) >= count;
    }

    // retire `count` items, en commencant par la hotbar puis l'inventaire
    public boolean consume(int id, int count) {
        if (!has(id, count)) return false;
        int remaining = count;
        for (int i = 0; i < 9 && remaining > 0; i++) {
            if (slot[i] != null && slot[i].getItemId() == id) {
                int take = Math.min(remaining, slot[i].getAmount());
                slot[i].remove(take);
                remaining -= take;
                if (slot[i].getAmount() <= 0) slot[i] = null;
            }
        }
        if (remaining > 0) {
            this.inventory.remove(id, remaining);
        }
        return true;
    }


	// Détecte les monstres dans la zone ciblée et leur inflige des dégâts
    private void attackEnemies(Level level, double targetX, double targetY) {
        // Arme tenue (-1 = poings nus)
        ItemStack weapon = getSelectedItem();
        int weaponId = (weapon != null) ? weapon.getItemId() : -1;

        // Dégâts de base (Poings nus) + bonus selon l'épée (système déjà voulu)
        int damage = 1;
        switch (weaponId) {
            case 200: damage = 2; break; // Épée en bois
            case 210: damage = 3; break; // Épée en pierre
            case 220: damage = 4; break; // Épée en fer
            case 230: damage = 5; break; // Épée en or
            case 240: damage = 6; break; // Épée en gem
        }

        // Coût en énergie du coup : main nue = 5, baisse avec le tier de l'épée (gem = 0)
        int cost = Tools.attackEnergyCost(weaponId);

        // Trop épuisé pour frapper : on vibre et on annule
        if (this.energy < cost) {
            triggerShake();
            return;
        }

        boolean hitSomething = false;

        // Frapper les zombies (Bots)
        for (Bot bot : level.getBots()) {
            if (isHit(bot.getX(), bot.getY(), targetX, targetY)) {
                bot.takeDamage(damage);
                hitSomething = true;
            }
        }

        // Frapper les archers
        if (level.getArcherBots() != null) {
            for (ArcherBot archer : level.getArcherBots()) {
                if (isHit(archer.getX(), archer.getY(), targetX, targetY)) {
                    archer.takeDamage(damage);
                    hitSomething = true;
                }
            }
        }

        // Frapper les moutons
        if (level.getSheepBots() != null) {
            for (SheepBot sheep : level.getSheepBots()) {
                if (isHit(sheep.getX(), sheep.getY(), targetX, targetY)) {
                    sheep.takeDamage(damage, level);
                    hitSomething = true;
                }
            }
        }

        // On ne dépense de l'énergie que si le coup a réellement touché une cible
        if (hitSomething) {
            this.loseEnergy(cost);
        }
    }

    /** Déclenche la petite animation de vibration (feedback d'échec). */
    private void triggerShake() {
        this.shakeTimer = SHAKE_DURATION;
    }

    // Vérifie si le monstre est assez proche de la case attaquée
    private boolean isHit(double botX, double botY, double targetX, double targetY) {
        double dx = (botX + Config.blockSize / 2.0) - (targetX + Config.blockSize / 2.0);
        double dy = (botY + Config.blockSize / 2.0) - (targetY + Config.blockSize / 2.0);
        double distance = Math.sqrt(dx * dx + dy * dy);
        
        // Si le centre du monstre est proche du centre de la case attaquée
        return distance < Config.blockSize * 1.5; 
    }

	public void loseEnergy(int amount) {
        this.energy -= amount;
        if (this.energy < 0) {
            this.energy = 0;
    }
}

	public int getHealth() { return health; }
	public int getMaxHealth() { return maxHealth; }
	public int getEnergy() { return energy; }

	public void setSelectedSlot(int selectedSlot) {
		if (selectedSlot>=0 && selectedSlot<=8) {
			this.selectedSlot=selectedSlot;
		}
	}

	public void consumeSelectedItem() {
        ItemStack currentItem = getSelectedItem();
        if (currentItem != null) {
            
            // 1. On utilise ta propre méthode pour enlever 1
            currentItem.remove(1); 
            
            // 2. On vérifie avec ton getter si la quantité est tombée à 0
            if (currentItem.getAmount() <= 0) {
                this.slot[selectedSlot] = null; // On vide la case
            }
        }
    }
	private ItemStack getSelectedItem(){ return slot[selectedSlot]; }
	// Slots d'armure du joueur
	private ArmorSlots armorSlots = new ArmorSlots();

	public ArmorSlots getArmorSlots() { return armorSlots; }

	public ItemStack[] getHotbar() { 
		return this.slot; 
	}

	public int getSelectedSlot() { 
		return this.selectedSlot; 
	}

	// Retourne les coordonnées du joueur en cases (Tiles) pour l'IA des monstres
    public int getTileX() {
        return (int) (getCenterX() / Config.blockSize);
    }

    public int getTileY() {
        return (int) (getCenterY() / Config.blockSize);
    }
}
