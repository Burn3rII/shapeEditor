Auteurs:
ANTROPIUS Simon
DESMONTEIX Maxence

Description de l'application:
Cette application se divise en 3 zones:
* Le menu, en haut de la fenêtre
* La zone de dessin, au centre
* La zone de texte pour l'aide utilisateur, en bas

Menu :
Contient les sections suivantes:
* "File" -> permet de sauvegarder, d'ouvrir un fichier ou de créer une nouvelle fenêtre/zone de dessin
* "Canvas" -> contient les opérations pouvant être réalisées sur la zone de dessin en général (pour l'instant, changer le fond).
* "Shapes" -> permet de réaliser des opérations sur les formes (création, composition, peinture).
* "Préférences" -> permet de choisir certains paramètres de l'application (pour l'instant, demander confirmation ou non en cas de fermeture de la fenêtre sans avoir sauvegardé).
* "Help" -> montre la fenêtre d'aide.

Zone de dessin:
* Pour les opérations lancées à partir du menu (création de forme, composition, peinture etc...), sélectionner l'opération voulue dans le menu puis suivre les instructions de l'aide utilisateur, située en bas de la fenêtre.
* Pour sélectionner une forme -> clic gauche sur celle-ci (en cas de formes superposées, celle au premier plan est sélectionnée).
* Une fois une forme sélectionnée : clic gauche, drag and drop pour bouger la forme ou la redimensionner (le curseur indique l'opération suivant l'emplacement de la souris dans la forme). Les points bleus sont des poignées de redimensionnement.
* Une fois une forme sélectionnée : clic droit à l'intérieur de celle-ci pour afficher le menu popup.
* Pour annuler une opération : clic gauche dans la zone de dessin sur un endroit où il n'y a pas de forme ou lancer une autre opération.

Menu popup:
* Color : choisir la couleur de la forme.
* Outline : choisir la couleur et l'épaisseur du contour de la forme.
* Depth : modifier la profondeur de la forme (par rapport aux autres).
* Symetry : effectuer une symétrie.
* Duplicate : dupliquer la forme.
* Delete : supprimer la forme.

(Concernant la fonctionnalité originale, nous aimerions être évalués sur la possibilité de changer la profondeur d'une forme.)

Exemple de bug ayant été pris en compte:
Lors du redimensionnement d'une forme, voici les étapes permettant de ne pas faire sortir la forme de la fenêtre et de pouvoir redimensionner en dépassant le bord opposé à celui redimensionné sans que cela ne pose de problème:
* Étape 1 : Si la forme se trouve au bord de la fenêtre, alors nous calculons une position corrigée de la souris par rapport à sa position réelle. Par exemple, si la position réelle de la souris indique qu'il faudrait faire sortir la forme de 20 pixels à gauche de la fenêtre, alors nous ajoutons 20 pixels à la composante x de la souris. Ainsi, la forme ne sort jamais de la fenêtre.
* Étape 2 : Si le déplacement dx de la souris depuis le dernier mouvement de la forme est supérieur à la largeur de cette dernière, cela signifie que la prochaine largeur de la forme sera < 0. Sans correction, la forme disparait. Pour régler ce problème, nous modifions le type de redimensionnement (par exemple en passant d'un redimensionnement à droite à un redimensionnement à gauche). Dans le même temps, nous translatons la forme en x d'une valeur égale à sa largeur afin que le changement de type de redimensionnement conserve la position initiale de la forme. (Raisonnement similaire en y et sur les coins.)
* Étape 3 : Après ces 2 vérifications, nous sommes sûrs que tout mouvement de l'utilisateur n'entrainera pas de problème : nous pouvons alors redimensionner la forme.