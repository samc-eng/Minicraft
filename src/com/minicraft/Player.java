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
	private int health = 10;
	private int energy = 10;
	private int invulnerabilityTimer = 3;
    private int selectedItemId = 1; // l'item actuellement en main (ID)
	private int selectedSlot = 0;
	private ItemStack[] slot = new ItemStack[9];

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
		if (input.isClicked(KeyCode.F1)) { setSelectedSlot(0); }
		if (input.isClicked(KeyCode.F2)) { setSelectedSlot(1); }
		if (input.isClicked(KeyCode.F3)) { setSelectedSlot(2); }
		if (input.isClicked(KeyCode.F4)) { setSelectedSlot(3); }
		if (input.isClicked(KeyCode.F5)) { setSelectedSlot(4); }
		if (input.isClicked(KeyCode.F6)) { setSelectedSlot(5); }
		if (input.isClicked(KeyCode.F7)) { setSelectedSlot(6); }
		if (input.isClicked(KeyCode.F8)) { setSelectedSlot(7); }
		if (input.isClicked(KeyCode.F9)) { setSelectedSlot(8); }


		
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

		if (input.isPressed(KeyCode.SHIFT) && energy>3) {
			this.vitesse=0.5;
		} else {
			this.vitesse=0.25;
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
        
        
        //on dessine l'animation
        if (flip) {
            gc.drawImage(this.skin, 
                    sourceX, sourceY, 16, 16,  
                    x+Config.blockSize, y, -Config.blockSize, Config.blockSize               
                );
        } else {
            gc.drawImage(this.skin, 
                    sourceX, sourceY, 16, 16,  
                    x, y, Config.blockSize, Config.blockSize               
                );
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
            // MODE DESTRUCTION
            int cibleBlock = level.getBlocks(cibleX * Config.blockSize, cibleY * Config.blockSize);
            if (cibleBlock != 0) {
                level.setBlocks(cibleX * Config.blockSize, cibleY * Config.blockSize, 0);
				this.loseEnergy(3);
            }
        } else {
            // MODE CONSTRUCTION
			ItemStack stackInHand = getSelectedItem();
			
			// Si la main n'est pas vide
			if (stackInHand != null) {
				ItemDefinition def = stackInHand.getDefinition();
				
				// Si l'objet est posable
				if (def != null && def.placeable) {
					
					// Hitbox du futur mur (tu avais très bien codé ça !)
					double bLeft = cibleX * Config.blockSize;
					double bRight = bLeft + Config.blockSize;
					double bTop = cibleY * Config.blockSize;
					double bBottom = bTop + Config.blockSize;
					
					boolean seTouchent = !(x+8 <= bRight || x + Config.blockSize-8 >= bLeft ||
										y+8 <= bBottom || y + Config.blockSize-8 >= bTop);
					
					// S'il n'y a pas de collision avec le joueur
					if (!seTouchent) {
						// 1. On pose le bloc sur la carte
						level.setBlocks(cibleX*Config.blockSize, cibleY*Config.blockSize, def.id);
						
						// 2. On utilise notre nouvelle méthode pour diminuer la quantité !
						consumeSelectedItem(); 
						
						System.out.println("Bloc [" + def.name + "] posé !");
					}
				} else {
					System.out.println("Cet objet n'est pas un bloc posable.");
				}
			}
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

	public void takeDamage(int amount) {
		if (invulnerabilityTimer > 0) return;
				int reduction = armorSlots.getArmorReduction();
				int degatsReels = Math.max(1, amount - reduction); // minimum 1 degat toujours
				health -= degatsReels;
		invulnerabilityTimer = 30;
		if (health <= 0) {
			health = 10;
			x = 50;
			y = 50;
		}
	}

	// Méthode unifiée pour ajouter un objet à la fois dans l'inventaire et la Hotbar
    public void pickUpItem(ItemStack stack) {
        // 1. On l'ajoute à l'inventaire de Rayan (pour que les recettes de craft le détectent)
        this.inventory.add(stack);
        
        // 2. On l'ajoute à notre Hotbar (pour l'affichage du HUD et la pose)
        int id = stack.getItemId();
        int amount = stack.getAmount();

        // A. On regarde si cet item est déjà dans la Hotbar pour s'empiler
        for (int i = 0; i < 9; i++) {
            if (slot[i] != null && slot[i].getItemId() == id && !slot[i].isFull()) {
                slot[i].add(amount);
                return;
            }
        }

        // B. Sinon, on cherche la première case vide (null)
        for (int i = 0; i < 9; i++) {
            if (slot[i] == null) {
                slot[i] = new ItemStack(id, amount);
                return;
            }
        }
    }

	public void loseEnergy(int amount) {
		energy -= amount;
		if (energy<=0) energy=10;
	}

	public int getHealth() { return health; }
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
}
