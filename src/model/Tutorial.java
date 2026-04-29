package src.model;

import src.view.Display;
import src.view.GameDialog;
import src.view.Global;

import javax.swing.*;
import static src.Main.frame;

/**
 * Classe utilitaire pour gérer le tutoriel du jeu.
 * Contient une série de dialogues explicatifs affiches au cours du debut de la partie.
 */
public class Tutorial {

    private static final boolean[] activeHotbarSlots = {true, true, true, true}; // Pour indiquer quels slots sont actifs
    private static boolean droughtEnabled = true; // Pour le tuto, on ne veut pas que les plantes meurent de soif
    public static boolean droughtWarningShown = false; // Pour éviter de spam le joueur avec des messages de sécheresse pendant le tuto

    public static boolean isDroughtEnabled() {
        return droughtEnabled;
    }

    public static boolean isHotbarSlotActive(int index) {
        if (index < 0 || index >= activeHotbarSlots.length) {
            throw new IllegalArgumentException("Index de slot invalide : " + index);
        }
        return activeHotbarSlots[index];
    }

    public static void setHotbarSlotActive(int index, boolean active) {
        if (index < 0 || index >= activeHotbarSlots.length) {
            throw new IllegalArgumentException("Index de slot invalide : " + index);
        }
        activeHotbarSlots[index] = active;
    }

    /** Message de bienvenue */
    public static void tuto1() {
        ChickenSpawner.setActive(false); // Désactiver le spawner de poules sauvages pendant le tutoriel pour éviter les distractions
        for (int i = 0; i < 4; i++) setHotbarSlotActive(i, false);
        droughtEnabled = false; // éviter que les plantes meurent de soif avant que le joueur ait appris à les arroser
        GameDialog.showMessage(frame, "Tutoriel (1/10)", "Bienvenue dans Saclay Valley !\n\n" +
                    "Vous venez d'heriter de la ferme de votre grand-pere dans la region de Saclay.\n" +
                    "Votre objectif est de relancer la ferme a l'aide de votre fidele jardinier. \n" +
                    "Cultivez des plantes, construisez des batiments, et decorez votre ferme pour la rendre plus belle et plus rentable !",
                Tutorial::tuto2
        );
    }

    /** Incite a poser la grange */
    public static void tuto2() {
        GameDialog.showMessage(frame, "Tutoriel (2/10)",
                "Pour commencer, il vous faudra une grange pour stocker vos produits.\n" +
                        "Pour cela, cliquez sur le marteau pour ouvrir le menu de construction, puis posez la grange."
        );
    }


    /** Explique les actions de base et incite le joueur a labourer sa premiere parcelle */
    public static void tuto3() {
        setHotbarSlotActive(0, true); // Activer le slot 1 (labourer) pour le tuto
        GameDialog.showMessage(frame, "Tutoriel (3/10)",
            "Maintenant que vous avez une grange, il est temps de cultiver votre premiere parcelle !\n" +
                    "Pour cela, il faut demander a votre jardinier de le faire. \n\n" +
                    "Commencez par fermer le menu de construction (bouton x ou touche esc)\n" +
                    "Vous aurez en bas de votre ecran une barre d'actions. \n" +
                    "Choisissez l'action 1 (labourer), puis selectionner 10 cases en faisant glisser votre souris \n" +
                    "pour lui demander de labourer une parcelle\n" +
                    "Confirmez votre selection avec la touche entree."
        );
    }

    /** Apres avoir labouré, incite a planter sur la parcelle */
    public static void tuto4() {
        setHotbarSlotActive(1, true); // Activer le slot 2 (arroser) pour le tuto
        setHotbarSlotActive(2, true); // Activer le slot 3 (planter) pour le tuto
        GameDialog.showMessage(frame, "Tutoriel (4/10)",
                "Bravo, vous avez laboure votre premiere parcelle !\n" +
                        "Maintenant, il est temps de la planter pour faire pousser vos premieres plantes.\n" +
                        "Choisissez l'action 3 (planter), puis choisissez les graines a planter (carottes), \n" +
                        "puis selectionnez la parcelle labouree (shift+clic pour tout selectionner d'un coup).\n" +
                        "Confirmez votre selection avec la touche entree."
        );
    }

    /** Apres avoir planté, incite a arroser la parcelle */
    public static void tuto5() {
        GameDialog.showMessage(frame, "Tutoriel (5/10)",
                "Parfait, vos carottes sont plantees !\n" +
                        "Pour les faire pousser, il faut les arroser regulierement.\n" +
                        "Ne les laissez pas sans eau trop longtemps car elles risquent de mourir, " +
                        "mais ne les innondez pas non plus!\n" +
                        "Choisissez l'action 2 (arroser), puis selectionnez la parcelle (shift+clic pour selectionner tout d'un coup).\n" +
                        "Confirmez votre selection avec la touche entree."
        );
    }

    /** Apres avoir arrose, parler des poulets */
    public static void tuto6() {
        ChickenSpawner.setActive(true); // Activer le spawner de poules sauvages pour le tuto
        GameDialog.showMessage(frame, "Tutoriel (6/10)",
                "Vos carottes sont maintenant en train de pousser!\n" +
                        "Mais attention, elle pourraient attirer l'attention des poulets sauvages qui viendront les manger !\n" +
                        "Heureusement, vous pouvez les chasser en cliquant dessus. Soyez vigilant!\n\n" +
                        "N'oubliez aussi pas de rearroser vos plantes si elles n'ont plus d'eau!");
    }

    /** Apres les poulets, parler de recolter */
    public static void tuto7() {
        setHotbarSlotActive(3, true); // Activer le slot 4 (recolter) pour le tuto
        GameDialog.showMessage(frame, "Tutoriel (7/10)",
                "Vos carottes sont bientot pretes a etre recoltees!\n" +
                        "Des qu'elles sont mures, choisissez l'action 4 (recolter), puis selectionnez leur parcelle.\n" +
                        "Le jardinier les recoltera et les enmenera a la grange."
        );
    }

    /** Apres la recolte, vendre le carottes */
    public static void tuto8() {
        GameDialog.showMessage(frame, "Tutoriel (8/10)",
                "Bravo, vous avez recolte vos premieres carottes !\n" +
                        "Maintenant, il est temps de les vendre pour gagner de l'argent.\n\n" +
                        "Une fois que le jardinier a depose les carottes recoltees a la grange,\n" +
                        "cliquez sur la grange pour ouvrir son menu de vente, puis vendez vos carottes.\n" +
                        "C'est aussi la-bas que vous pourrez acheter de nouvelles graines."
        );
    }

    /** Apres avoir vendu, parler de la destruction de batiments */
    public static void tuto9() {
        droughtEnabled = true; // réactiver la sécheresse pour le reste du jeu
        GameDialog.showMessage(frame, "Tutoriel (9/10)",
                "Maintenant que vous avez vendu vos produits, vous avez de l'argent pour investir dans votre ferme !\n" +
                        "Vous pouvez construire de nouveaux batiments utiles, ou decorer votre ferme pour la rendre plus belle.\n" +
                        "Mais il faudrait d'abord faire un peu de place!\n\n" +
                        "Pour cela, vous pouvez detruire des batiments et obstacles en cliquant sur la poubelle a droite de l'ecran,\n" +
                        "puis en selectionnant les batiments et obstacles a detruire (faites glisser la souris pour en selectionner plusieurs).\n" +
                        "Clic droit ou esc pour sortir du mode suppression\n\n" +
                        "Commencez par en detruire 5."
        );
    }

    /** Apres avoir detruit, parler de barrieres */
    public static void tuto10() {
        GameDialog.showMessage(frame, "Tutoriel (10/10)",
            "Parfait, votre ferme est maintenant plus propre, vous pouvez construire de nouveaux batiments!\n" +
                    "Ouvrez le menu de construction et regardez les batiments disponibles.\n" +
                    "Vous y trouverez des barrieres pour proteger vos cultures des poulets sauvages.\n" +
                    "Essayez d'en construire quelques unes pour entourer votre parcelle de carottes.\n\n" +
                    "Mais n'oubliez pas qu'elles coutent de l'argent, faites attention a en garder suffisamment\n" +
                    "pour acheter de nouvelles graines!"
        );
    }

    /** Apres avoir fini le tuto, incite a aller voir les quetes */
    public static void tuto11() {
        GameDialog.showMessage(frame, "Tutoriel (fin)",
                "Bravo, vous avez fini le tutoriel!\n" +
                        "Vous avez debloque de nouvelles quetes, qui vous guideront dans la suite de votre aventure.\n" +
                        "Vous povez aller les consulter en cliquant sur le parchemin a droite de l'ecran. \n" +
                        "Ces quetes vous permettront aussi de gagner de l'argent et de l'experience pour progresser plus rapidement dans le jeu. \n\n" +
                        "Bonne chance pour la suite de votre aventure dans Saclay Valley!"
        );
    }
}
