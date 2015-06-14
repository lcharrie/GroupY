package tangibleGame;

import processing.core.PApplet;

public class Start {
	TangibleGame parent;
	boolean enable = true;
	boolean instructions = false;
	boolean params = false;
	boolean started[] = { false, false, false };
	int availableFood = 0;
	boolean free = false;
	int requiredFood = 0;
	int level = 1;

	public Start(TangibleGame p) {
		parent = p;
	}

	public void display() {
		parent.fill(255, 255, 255);
		if (enable) {
			parent.textSize(36);
			parent.text(
					"Selectionnez au clavier : \n    N - Niveaux\n    R - pRédéfini\n    L - Mode Libre\n    I - Instructions\n    P - Paramètres",
					-200, -400, 0);
		} else if (instructions) {
			parent.textSize(36);
			parent.text("Règle du jeu :", -400, -400, 0);
			parent.textSize(24);
			parent.text(
					"La chenille doit se dépêcher de faire une réserve de nourriture \navant de pouvoir s’envoler avec la montgolfière. \nPour cela elle doit :",
					-400, -350, 0);
			parent.text(
					"    -  S’approcher des fruits et les manger. Ces fruits diminuent\n        de taille après avoir été dégustés, jusqu’à disparaître.\n    -  Répéter l’opération pour avoir suffisamment de réserve. \n        Attention à ne pas toucher les bords.\n    -  Puis se diriger vers la montgolfière et s’envoler vers \n        de nouvelles aventures !",
					-400, -240, 0);
			parent.textSize(36);
			parent.text("Les différents niveaux :", -400, 0, 0);
			parent.textSize(24);
			parent.text(
					"    -  Niveaux : Niveaux à difficulté variable\n    -  pRédéfini : Niveau à paramètre prédéfini dans les paramètres\n    -  Libre : Les fruits sont placés de manière libre par l’utilisateur \n        et à volonté.",
					-400, 50, 0);
			parent.textSize(36);
			parent.text("Paramètres :", -400, 250, 0);
			parent.textSize(24);
			parent.text("    -  Score minimum (free mode)", -400, 300, 0);
			parent.textSize(24);
			parent.text("Presser SUPPRIMER pour retourner au menu principal",
					-300, 430, 0);
		} else if (params) {
			parent.textSize(36);
			parent.text("Mode Libre :", -400, -400, 0);
			parent.textSize(24);
			parent.text(
					"    1 - Portions disponibles (presser 1 puis votre nombre et ENTRER) : ",
					-400, -350, 0);
			if (started[0])
				parent.fill(255, 0, 0);
			parent.text("          Actuellement : " + availableFood
					+ " portions disponibles", -400, -300, 0);
			parent.fill(255, 255, 255);
			parent.text(
					"    2 - Portitions nécessaire pour finir le niveau (presser 2) : ",
					-400, -250, 0);
			if (started[1])
				parent.fill(255, 0, 0);
			parent.text("          Actuellement : " + requiredFood
					+ " portions requises", -400, -200, 0);
			parent.fill(255, 255, 255);
			parent.text(
					"    3 - Niveau de difficulté 0 < votre niveau < 10 (presser 3) : ",
					-400, -150, 0);
			if (started[2])
				parent.fill(255, 0, 0);
			parent.text("          Actuellement : niveau " + level, -400, -100,
					0);
			parent.fill(255, 255, 255);
			parent.text("Presser SUPPRIMER pour retourner au menu principal",
					-300, 430, 0);
		}
	}

	public void action() {
		if (parent.startMode) {
			if (parent.key == PApplet.BACKSPACE) {
				enable = true;
				instructions = false;
				params = false;
				started[0] = false;
				started[1] = false;
			} else if (params) {
				if (started[0]) {
					if (parent.key == PApplet.ENTER) {
						started[0] = false;
					} else if (parent.key >= '0' && parent.key <= '9') {
						PApplet.println(parent.key);
						availableFood = availableFood * 10 + parent.key - 48;
					}
				} else if (started[1]) {
					if (parent.key == PApplet.ENTER) {
						started[1] = false;
					} else if (parent.key >= '0' && parent.key <= '9') {
						requiredFood = requiredFood * 10 + parent.key - 48;
					}
				} else if (started[2]) {
					if (parent.key == PApplet.ENTER) {
						started[2] = false;
					} else if (parent.key > '0' && parent.key <= '9') {
						level = parent.key - 48;
					}
				} else if (parent.key == '1') {
					started[0] = true;
					availableFood = 0;
				} else if (parent.key == '2') {
					started[1] = true;
					requiredFood = 0;
				} else if (parent.key == '3') {
					started[2] = true;
				}
			} else {
				switch (parent.key) {
				case 'n':
					level();
					break;
				case 'r':
					custom();
					break;
				case 'l':
					free();
					break;
				case 'i':
					instructions();
					break;
				case 'p':
					params();
					break;
				}
			}
		}
	}

	private void level() {
		parent.shiftModeEnable = false;
		parent.setup();
		parent.cylindersCollection.addWhile((int) (37.5 * level + 162.5)); // tout
																			// plein
		parent.minimalScore = (int) (56.25 * level - 6.25);
		parent.startMode = false;
	}

	private void custom() {
		parent.shiftModeEnable = false;
		parent.setup();
		parent.cylindersCollection.addWhile(Math.max(requiredFood,
				availableFood)); // tout plein mais d'autres que
		parent.minimalScore = requiredFood;
		parent.startMode = false;
	}

	private void free() {
		free = true;
		parent.setup();
		parent.shiftModeEnable = true;
		parent.minimalScore = 200;
		parent.startMode = false;
	}

	private void instructions() {
		enable = false;
		instructions = true;
		params = false;
	}

	private void params() {
		enable = false;
		instructions = false;
		params = true;
	}
}
