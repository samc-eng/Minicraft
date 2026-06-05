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
	private int invulnerabilityTimer = 0;
	private int selectedSlot = 0;
	private ItemStack[] slot = new ItemStack[9];
	private boolean isSwimming = false;

	public boolean up;
	public boolean down;
	public boolean right;
	public boolean left;
	
	public Player(double startX, double startY) {
		this.x=startX;
		this.y=startY;
		this.vitesse=0.25;

		try {
			this.skin=new Image("file:resources/skins.png");
		} catch (Exception e){
			System.out.println("Erreur : impossible de charger le skin");
		}
		
	}
	
	public void tick(Level level, InputHandler input) {
		this.isMoved=false;
		double futurX=x;
		double futurY=y;
		
		if (input.isPressed(KeyCode.Z)) {futurY-=vitesse;dir=1;isMoved=true;}
		if (input.isPressed(KeyCode.S)) {futurY+=vitesse;dir=0;isMoved=true;}
		if (input.isPressed(KeyCode.D)) {futurX+=vitesse;dir=3;isMoved=true;}
		if (input.isPressed(KeyCode.Q)) {futurX-=vitesse;dir=2;isMoved=true;}

		// --- SÉLECTION DE LA HOTBAR ---
		if (input.isClicked(KeyCode.DIGIT1) || input.isClicked(KeyCode.F1)) { setSelectedSlot(0); }
		if (input.isClicked(KeyCode.DIGIT2) || input.isClicked(KeyCode.F2)) { setSelectedSlot(1); }
		if (input.isClicked(KeyCode.DIGIT3) || input.isClicked(KeyCode.F3)) { setSelectedSlot(2); }
		if (input.isClicked(KeyCode.DIGIT4) || input.isClicked(KeyCode.F4)) { setSelectedSlot(3); }
		if (input.isClicked(KeyCode.DIGIT5) || input.isClicked(KeyCode.F5)) { setSelectedSlot(4); }
		if (input.isClicked(KeyCode.DIGIT6) || input.isClicked(KeyCode.F6)) { setSelectedSlot(5); }
		if (input.isClicked(KeyCode.DIGIT7) || input.isClicked(KeyCode.F7)) { setSelectedSlot(6); }
		if (input.isClicked(KeyCode.DIGIT8) || input.isClicked(KeyCode.F8)) { setSelectedSlot(7); }
		if (input.isClicked(KeyCode.DIGIT9) || input.isClicked(KeyCode.F9)) { setSelectedSlot(8); }


		
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

		this.isSwimming=this.isInWater(level);
		
		if (this.isSwimming && input.isPressed(KeyCode.SHIFT) && energy>3){
			this.vitesse=1.2;
		} else if (this.isSwimming){
			this.vitesse=0.7;
		} else if (input.isPressed(KeyCode.SHIFT) && energy>3) {
			this.vitesse=1.5;
		} else {
			this.vitesse=1;
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
        
        
        // on dessine l'animation
        if (this.isSwimming) {
            // --- DANS L'EAU : On ne prend que 8 pixels de haut (la moitié du skin) ---
            // On décale aussi le dessin vers le bas (+ Config.blockSize / 2) pour l'enfoncer
            if (flip) {
                gc.drawImage(this.skin, 
                        sourceX, sourceY, 16, 8,  
                        x + Config.blockSize, y + (Config.blockSize / 2.0), -Config.blockSize, Config.blockSize / 2.0               
                    );
            } else {
                gc.drawImage(this.skin, 
                        sourceX, sourceY, 16, 8,  
                        x, y + (Config.blockSize / 2.0), Config.blockSize, Config.blockSize / 2.0               
                    );
            }
        } else {
            // --- SUR TERRE : Dessin normal entier (16 pixels) ---
            if (flip) {
                gc.drawImage(this.skin, 
                        sourceX, sourceY, 16, 16,  
                        x + Config.blockSize, y, -Config.blockSize, Config.blockSize               
                    );
            } else {
                gc.drawImage(this.skin, 
                        sourceX, sourceY, 16, 16,  
                        x, y, Config.blockSize, Config.blockSize               
                    );
            }
        }

        if (isInvulnerable()
                && (invulnerabilityTimer / Config.PLAYER_DAMAGE_BLINK_TICKS) % 2 == 0) {
            gc.setFill(Color.rgb(255, 40, 40, 0.55));
            gc.fillRect(x, y, Config.blockSize, Config.blockSize);
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
	
	// Dans Player.java
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
            // MODE DESTRUCTION (Blocs)
            int cibleBlock = level.getBlocks(cibleX * Config.blockSize, cibleY * Config.blockSize);
            if (cibleBlock != 0) {
                level.setBlocks(cibleX * Config.blockSize, cibleY * Config.blockSize, 0);
                this.loseEnergy(3);
            }
            
            // --- NOUVEAU : MODE COMBAT (Monstres) ---
            attackEnemies(level, cibleX * Config.blockSize, cibleY * Config.blockSize);
        } else {
            // MODE CONSTRUCTION
			ItemStack stackInHand = getSelectedItem();

			if (stackInHand == null) {
				System.out.println("Aucun item sélectionné dans la hotbar.");
				return;
			}

			ItemDefinition def = stackInHand.getDefinition();
			if (def == null || !def.placeable) {
				System.out.println("Cet objet n'est pas un bloc posable.");
				return;
			}

			// Vérifie qu'il n'y a pas déjà un bloc dans la case cible
			int existing = level.getBlocks(cibleX * Config.blockSize, cibleY * Config.blockSize);
			if (existing != 0) {
				System.out.println("Impossible de poser : la case est déjà occupée (bloc " + existing + ").");
				return;
			}

			// On pose le bloc et on consomme l'item
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

        setHealth(this.health - damage);
        this.invulnerabilityTimer = Config.PLAYER_INVULNERABILITY_TICKS;
        System.out.println("Aie ! Le joueur a pris " + damage + " degats. Vie restante : " + this.health);
        return true;
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

    // Ajoute un item à la possession du joueur : hotbar d'abord (empilage puis case vide),
    // puis inventaire si la hotbar est pleine. Aucune duplication.
    public void pickUpItem(ItemStack stack) {
        int id     = stack.getItemId();
        int amount = stack.getAmount();

        for (int i = 0; i < 9; i++) {
            if (slot[i] != null && slot[i].getItemId() == id && !slot[i].isFull()) {
                slot[i].add(amount);
                return;
            }
        }
        for (int i = 0; i < 9; i++) {
            if (slot[i] == null) {
                slot[i] = new ItemStack(id, amount);
                return;
            }
        }
        this.inventory.add(stack);
    }

    // Total disponible (hotbar + inventaire) pour un item donné.
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

    // Consomme `count` unités de `id` (hotbar d'abord, puis inventaire). Retourne false si insuffisant.
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
        // Dégâts de base (Poings nus)
        int damage = 1;
        
        // Bonus de dégâts si tu as une épée en main
        ItemStack weapon = getSelectedItem();
        if (weapon != null) {
            int id = weapon.getItemId();
            if (id == 200) damage = 2; // Épée en bois
			if (id == 210) damage = 3; // Épée en pierre
            if (id == 220) damage = 4; // Épée en fer
            if (id == 230) damage = 5; // Épée en or
            if (id == 240) damage = 6; // Épée en gem
        }

        // Frapper les zombies (Bots)
        for (Bot bot : level.getBots()) {
            if (isHit(bot.getX(), bot.getY(), targetX, targetY)) {
                bot.takeDamage(damage);
            }
        }

        // Frapper les archers
        if (level.getArcherBots() != null) {
            for (ArcherBot archer : level.getArcherBots()) {
                if (isHit(archer.getX(), archer.getY(), targetX, targetY)) {
                    archer.takeDamage(damage);
                }
            }
        }
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
		energy -= amount;
		if (energy<=0) energy=10;
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
