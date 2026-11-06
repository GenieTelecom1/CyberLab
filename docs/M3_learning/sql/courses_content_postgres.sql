-- CyberLab : contenu des 6 cours (M3 - Learning), version PostgreSQL.
-- Table Courses : id, titre, description, contenu, ordre.
-- Chaque cours est découpé en parties : une ligne "## Titre de la partie" puis son texte.
-- INSERT ... ON CONFLICT (id) DO UPDATE : le script peut être relancé sans doublons
-- et sans toucher à la progression déjà enregistrée. Les id 1 à 6 sont fixes.
-- OVERRIDING SYSTEM VALUE : permet d'imposer les id 1 à 6 même si la colonne id est une colonne automatique (identity) de M1.
-- Prérequis : la table Courses a une clé primaire sur id.


INSERT INTO Courses (id, titre, description, contenu, ordre)
OVERRIDING SYSTEM VALUE VALUES (
1,
'Introduction à la cybersécurité',
'Les bases : menaces, vocabulaire, triade CIA et principes de défense.',
'## Qu’est-ce que la cybersécurité ?
La cybersécurité (ou sécurité informatique) est l’ensemble des moyens techniques, organisationnels et humains qui servent à protéger les systèmes informatiques (ordinateurs, téléphones, serveurs, réseaux, applications) et surtout les données qu’ils contiennent contre les accès non autorisés, le vol, la modification, la destruction ou l’indisponibilité.

Elle ne concerne pas seulement les informaticiens. Dès que vous avez un compte e-mail, un compte sur un réseau social, une application bancaire ou un téléphone, vous êtes à la fois un utilisateur et une cible potentielle.

La cybersécurité repose sur trois piliers qui travaillent ensemble :
- Les technologies : pare-feu, antivirus, chiffrement, sauvegardes, mises à jour.
- Les processus : règles d’accès, procédures de vérification, plan de réaction en cas d’incident.
- Les personnes : utilisateurs formés qui savent reconnaître une situation anormale.

Exemple : un cadenas sur une porte (technologie) ne sert à rien si personne ne le ferme (comportement) et s’il n’existe aucune règle sur qui a la clé (processus).

En résumé : la cybersécurité protège les systèmes et les données, et elle dépend autant des gens que des outils.

## Pourquoi c’est important
Nous vivons une grande partie de notre vie en ligne : messages privés, photos, cours, documents administratifs, argent. Tout cela a de la valeur, pour nous comme pour les attaquants.

Ce que peut coûter une attaque réussie :
- Vol d’identité : quelqu’un utilise vos informations pour ouvrir des comptes ou escroquer vos proches.
- Perte d’argent : accès à un compte bancaire, achats frauduleux, rançon.
- Perte de données : travaux de fin d’études, photos, documents effacés ou chiffrés.
- Atteinte à la réputation : un compte piraté qui publie des messages à votre place.
- Interruption de service : un site ou un hôpital qui ne peut plus fonctionner.

Cas réel : en mai 2017, le ransomware WannaCry s’est propagé en quelques jours à plus de 150 pays et a touché des centaines de milliers d’ordinateurs, dont ceux d’hôpitaux au Royaume-Uni. Il exploitait une faille de Windows pour laquelle un correctif existait déjà depuis plusieurs semaines : les machines non mises à jour ont été les victimes.

Un point important : les attaquants ne choisissent pas toujours leur victime. Beaucoup d’attaques sont automatiques et lancées en masse. Être « petit » ou « inconnu » ne protège pas.

En résumé : une seule faille peut coûter cher, et beaucoup d’attaques sont automatiques et visent tout le monde.

## Le vocabulaire de base
Pour comprendre la suite, il faut connaître quelques mots précis.

- Actif : tout ce qui a de la valeur et doit être protégé (données, ordinateur, compte, réputation).
- Menace : ce qui peut causer un dommage (un pirate, un virus, une erreur humaine, une panne, un incendie).
- Vulnérabilité : une faiblesse qu’une menace peut exploiter (logiciel pas à jour, mot de passe faible, utilisateur non formé).
- Attaque : l’action concrète qui exploite une vulnérabilité.
- Exploit : un morceau de code ou une méthode qui profite d’une vulnérabilité précise.
- Correctif (patch) : une mise à jour qui répare une vulnérabilité.
- Faille « zero-day » : vulnérabilité inconnue de l’éditeur, donc sans correctif disponible au moment où elle est exploitée.
- Risque : la probabilité qu’une menace exploite une vulnérabilité, multipliée par l’impact que cela aurait.

On raisonne souvent ainsi : risque = probabilité x impact. Un mot de passe faible sur un compte de jeu a un impact limité. Le même mot de passe sur votre e-mail principal a un impact énorme, car l’e-mail permet de réinitialiser tous vos autres comptes.

Les types de « hackers » : le « white hat » teste la sécurité avec autorisation pour aider à la renforcer, le « black hat » attaque dans un but malveillant ou lucratif, le « grey hat » agit sans autorisation mais sans intention criminelle claire. Ce cours est celui d’un défenseur : on apprend les attaques pour mieux s’en protéger.

En résumé : une menace exploite une vulnérabilité pour atteindre un actif, et le risque dépend de la probabilité et de l’impact.

## La triade CIA
Toute la sécurité de l’information repose sur trois objectifs, qu’on appelle la triade CIA.

1. Confidentialité (Confidentiality) : seules les personnes autorisées peuvent accéder à l’information.
   Moyens : mots de passe, chiffrement, contrôle d’accès.
   Atteinte : vos messages privés sont lus par un inconnu, une base de données d’utilisateurs est volée.

2. Intégrité (Integrity) : l’information est exacte et n’est pas modifiée sans autorisation.
   Moyens : droits d’écriture limités, signatures numériques, sauvegardes, journaux.
   Atteinte : quelqu’un change une note dans un système scolaire, un virement est modifié pendant son envoi.

3. Disponibilité (Availability) : l’information et les services sont accessibles quand on en a besoin.
   Moyens : sauvegardes, serveurs redondants, protection contre les attaques par saturation.
   Atteinte : un site d’inscription tombe le jour de la date limite, un ransomware bloque vos fichiers.

Un même incident peut toucher plusieurs piliers. Un ransomware chiffre vos fichiers : la disponibilité est atteinte. S’il a aussi copié les fichiers avant de les chiffrer, la confidentialité l’est aussi.

Les trois objectifs peuvent entrer en tension. Verrouiller très fort un système protège la confidentialité mais peut nuire à la disponibilité. Le travail de sécurité consiste à trouver le bon équilibre selon la valeur de ce qu’on protège.

En résumé : confidentialité, intégrité, disponibilité : pour chaque incident, demandez-vous lequel des trois a été touché.

## Identifier, authentifier, autoriser
Quand vous vous connectez à un service, trois étapes distinctes ont lieu.

- Identification : vous dites qui vous êtes (votre adresse e-mail ou nom d’utilisateur).
- Authentification : vous le prouvez (mot de passe, code, empreinte).
- Autorisation : le système décide ce que vous avez le droit de faire (lire, modifier, administrer).

On ajoute souvent la traçabilité : le système garde une trace de qui a fait quoi, pour pouvoir enquêter après un incident.

Il existe trois grandes familles de preuves, appelées facteurs d’authentification :
1. Ce que vous savez : mot de passe, code PIN.
2. Ce que vous possédez : téléphone, clé de sécurité, carte.
3. Ce que vous êtes : empreinte digitale, visage.

L’authentification multifacteur (MFA, ou double authentification 2FA) combine au moins deux familles différentes. Si un attaquant vole votre mot de passe, il lui manque encore votre téléphone : l’attaque échoue ou devient bien plus difficile.

Le principe du moindre privilège : chaque personne ou programme ne reçoit que les droits nécessaires à sa tâche, rien de plus. Exemple : un étudiant n’a pas besoin des droits d’administrateur sur le serveur de l’université. Si son compte est piraté, les dégâts restent limités.

En résumé : on s’identifie, on s’authentifie, puis le système autorise. Plusieurs facteurs et le moindre privilège limitent les dégâts.

## Les grandes familles de menaces
Voici les menaces que vous allez étudier dans les prochains cours.

- Phishing : faux messages qui vous poussent à donner vos informations ou à cliquer. C’est la porte d’entrée la plus courante.
- Malware : logiciels malveillants (virus, chevaux de Troie, ransomware, logiciels espions).
- Ingénierie sociale : manipulation psychologique d’une personne pour obtenir une information ou une action, au téléphone, en face à face ou en ligne.
- Attaques réseau : écoute du trafic, faux Wi-Fi, saturation de services.
- Mots de passe faibles ou volés : deviner, réutiliser ou voler des identifiants.
- Erreurs humaines et configuration : envoyer un fichier à la mauvaise personne, laisser un service ouvert sans protection.
- Menaces internes : une personne autorisée qui abuse de ses droits, volontairement ou non.

Ces menaces s’enchaînent souvent. Exemple : un e-mail de phishing (menace 1) contient un fichier piégé qui installe un logiciel espion (menace 2) ; celui-ci vole un mot de passe (menace 5) qui sert à entrer dans un compte, puis l’attaquant écrit à vos contacts en se faisant passer pour vous (menace 3).

Voilà pourquoi la défense doit aussi être en plusieurs couches.

En résumé : phishing, malware, ingénierie sociale, réseau et mots de passe sont les cinq grandes portes d’entrée, et elles se combinent.

## Les principes de défense
Quelques idées simples guident toute bonne sécurité.

1. Défense en profondeur : empiler plusieurs protections. Si une couche tombe, la suivante arrête l’attaque. Exemple : mot de passe + double authentification + antivirus + sauvegardes.
2. Moindre privilège : donner uniquement les droits nécessaires.
3. Mises à jour régulières : la plupart des attaques exploitent des failles connues, déjà corrigées.
4. Séparer : ne pas mélanger usages sensibles et risqués (compte bancaire, jeux, sites douteux), comptes distincts, réseaux distincts.
5. Ne pas faire confiance par défaut : vérifier avant de croire un message, un appel, un lien ou un fichier.
6. Sauvegarder : une copie récente de vos données rend la plupart des incidents supportables.
7. Simplicité : une règle simple qu’on applique vaut mieux qu’une règle parfaite qu’on abandonne.
8. Se préparer à l’incident : savoir à l’avance quoi faire (qui prévenir, quels mots de passe changer).

Aucune protection n’est parfaite. L’objectif n’est pas le risque zéro, mais de rendre l’attaque difficile et coûteuse, et de limiter les dégâts si elle réussit. Un attaquant opportuniste choisit la cible la plus facile : en étant correctement protégé, vous n’êtes plus celle-là.

En résumé : plusieurs couches, peu de droits, mises à jour, sauvegardes et esprit de vérification.

## Les bons réflexes au quotidien
Une liste de gestes simples qui bloquent une grande partie des attaques courantes.

Comptes :
- Un mot de passe long et unique par compte (un gestionnaire de mots de passe aide).
- La double authentification activée sur l’e-mail, la banque, les réseaux sociaux.
- Un e-mail de récupération et un numéro de téléphone à jour.

Appareils :
- Mises à jour automatiques activées (système et applications).
- Verrouillage d’écran avec code ou empreinte.
- Applications installées uniquement depuis les boutiques officielles.
- Antivirus actif, pare-feu activé.

Messages et navigation :
- Ne jamais cliquer sur un lien reçu de façon inattendue, aller sur le site en tapant l’adresse.
- Ne jamais donner un mot de passe ou un code reçu par SMS, à personne.
- Se méfier de l’urgence, des menaces, des cadeaux trop beaux.
- Faire attention au Wi-Fi public.

Données :
- Sauvegardes régulières, y compris hors de l’appareil.
- Partager le moins possible d’informations personnelles en public.

En cas de doute, la meilleure décision est presque toujours de ne rien faire tout de suite, de vérifier par un autre canal, puis de décider.

En résumé : comptes solides, appareils à jour, esprit critique et sauvegardes.

## À retenir et auto-évaluation
Ce que vous avez appris dans ce cours :
- La cybersécurité protège les systèmes et les données, avec des outils, des règles et des comportements.
- Menace, vulnérabilité, risque : la menace exploite la vulnérabilité, le risque dépend de la probabilité et de l’impact.
- Triade CIA : confidentialité, intégrité, disponibilité.
- Identification, authentification, autorisation, et plusieurs facteurs pour renforcer la preuve.
- Principes : défense en profondeur, moindre privilège, mises à jour, sauvegardes.

Testez-vous (réponses après chaque question) :

Q1. Un ransomware chiffre tous les fichiers d’une entreprise. Quel pilier de la triade CIA est d’abord touché ?
Réponse : la disponibilité, car les fichiers ne sont plus accessibles. La confidentialité l’est aussi si les fichiers ont été copiés avant.

Q2. Quelle est la différence entre une menace et une vulnérabilité ?
Réponse : la menace est ce qui peut causer un dommage (un pirate, un virus). La vulnérabilité est la faiblesse exploitée (logiciel pas à jour, mot de passe faible).

Q3. Pourquoi la double authentification protège-t-elle même si votre mot de passe est volé ?
Réponse : l’attaquant doit en plus posséder un deuxième facteur, par exemple votre téléphone.

Q4. Un étudiant doit-il avoir les droits d’administrateur sur le serveur de l’université ? Pourquoi ?
Réponse : non, c’est le principe du moindre privilège : si son compte est piraté, les dégâts restent limités.

Q5. Pourquoi une attaque « zero-day » est-elle plus dangereuse ?
Réponse : parce qu’aucun correctif n’existe encore au moment où elle est exploitée.

Vous avez terminé le cours. Passez au cours Phishing pour étudier la porte d’entrée la plus courante.',
1)
ON CONFLICT (id) DO UPDATE SET
    titre = EXCLUDED.titre,
    description = EXCLUDED.description,
    contenu = EXCLUDED.contenu,
    ordre = EXCLUDED.ordre;


INSERT INTO Courses (id, titre, description, contenu, ordre)
OVERRIDING SYSTEM VALUE VALUES (
2,
'Phishing',
'Reconnaître et analyser les e-mails, SMS et liens frauduleux, et savoir réagir.',
'## Qu’est-ce que le phishing ?
Le phishing (hameçonnage en français) est une technique d’arnaque où l’attaquant se fait passer pour une entité de confiance (banque, administration, opérateur téléphonique, réseau social, université, collègue, livraison de colis) pour vous pousser à agir : cliquer sur un lien, ouvrir un fichier, donner un mot de passe, un code ou des informations bancaires.

Le mot vient de « fishing » (pêche) : l’attaquant lance des appâts en masse et attend que quelqu’un morde.

Un message de phishing cherche presque toujours à obtenir l’une de ces choses :
- Vos identifiants (e-mail, réseaux sociaux, compte bancaire).
- Vos données de paiement.
- L’installation d’un logiciel malveillant via une pièce jointe ou un lien.
- Un virement ou un paiement, souvent présenté comme urgent.

Le phishing est la porte d’entrée la plus courante des cyberattaques, parce qu’il est peu coûteux : l’attaquant n’a pas besoin de trouver une faille technique, il suffit qu’une seule personne se trompe.

Exemple : « Votre colis est en attente, réglez 1,99 € de frais de douane pour le recevoir. » Le montant est petit pour que la victime ne se méfie pas, mais le but réel est de récupérer les données de votre carte bancaire.

En résumé : le phishing exploite la confiance et la précipitation, pas une faille technique.

## Pourquoi ça marche
Les attaquants connaissent la psychologie. Un message de phishing joue presque toujours sur un ou plusieurs de ces leviers :

- L’urgence : « votre compte sera bloqué dans 24 heures ». On agit vite, on réfléchit peu.
- La peur : menace de fermeture de compte, d’amende, de piratage.
- L’autorité : le message semble venir de la banque, de la police, de l’administration, du directeur.
- La curiosité : « Photos de vous, regardez ! », « Vous avez gagné ».
- L’appât du gain : remboursement, cadeau, offre d’emploi trop belle.
- L’habitude : on reçoit réellement des messages de ce type, donc on ne se méfie pas.

Autre facteur : beaucoup de personnes lisent leurs messages sur téléphone, vite, en faisant autre chose. L’adresse de l’expéditeur est partiellement cachée, les liens sont difficiles à vérifier.

Contrairement à ce qu’on croit, les victimes ne sont pas « naïves ». Les messages modernes sont bien écrits, avec les bons logos et même des informations personnelles obtenues ailleurs. Même les spécialistes se font piéger quand ils sont fatigués ou pressés.

La meilleure protection est donc une habitude : ralentir et vérifier avant d’agir, surtout quand on se sent poussé à aller vite.

En résumé : le phishing joue sur nos émotions. Quand un message vous presse ou vous fait peur, c’est un signal d’alerte.

## Les formes de phishing
Le phishing n’est pas seulement un e-mail.

- Phishing par e-mail : envoi en masse, message générique.
- Spear phishing : message ciblé, rédigé pour une personne ou un petit groupe, avec des détails réels (votre nom, votre poste, un projet en cours).
- Whaling : spear phishing visant un dirigeant ou une personne influente.
- Fraude au président (BEC, Business Email Compromise) : un faux message de la direction demande un virement urgent ou un changement de coordonnées bancaires.
- Smishing : phishing par SMS ou messagerie (WhatsApp, Messenger). Exemple : faux SMS de livraison, de banque, d’opérateur.
- Vishing : phishing par appel téléphonique. « Bonjour, je suis du service sécurité de votre banque. »
- Quishing : le lien est caché dans un QR code, souvent collé sur une affiche ou envoyé par e-mail.
- Faux sites : copies de sites connus pour récupérer ce que vous saisissez.
- Clone phishing : l’attaquant copie un vrai e-mail que vous avez déjà reçu, remplace le lien ou la pièce jointe, et le renvoie.
- Phishing sur les réseaux sociaux : faux comptes, faux concours, messages d’un ami dont le compte a été piraté.

Dans votre contexte, les arnaques les plus fréquentes imitent souvent un opérateur de téléphonie, une banque, un service de livraison, une plateforme d’emploi ou de bourse, et même un membre de l’établissement universitaire.

En résumé : e-mail, SMS, appel, QR code ou réseau social, le principe est identique : un faux message pour vous faire agir.

## Anatomie d’un e-mail de phishing
Prenez l’habitude de lire un message en cinq étapes.

1. L’expéditeur. Regardez l’adresse complète, pas seulement le nom affiché. Le nom « Service Client » peut cacher une adresse sans rapport avec l’entreprise. Sur téléphone, appuyez sur le nom pour voir l’adresse.
2. L’objet. Beaucoup d’objets jouent sur l’urgence : « Dernier avertissement », « Action requise », « Compte suspendu ».
3. Le texte. Cherchez : salutation générique (« Cher client »), fautes ou tournures étranges, demande d’action immédiate, menace ou récompense.
4. Les liens. Ne cliquez pas. Survolez-les avec la souris (ou appui long sur mobile) pour voir la vraie destination. Elle doit correspondre au site officiel.
5. Les pièces jointes. Méfiez-vous d’un fichier inattendu : .zip, .exe, .scr, .js, ou un document Office qui demande « d’activer les macros ».

Question à vous poser à chaque fois : est-ce que j’attendais ce message ? Est-ce que cette entreprise me contacterait vraiment de cette façon pour cette raison ?

Un vrai organisme sérieux ne demande jamais par e-mail ou SMS votre mot de passe complet, votre code de carte, ou un code de vérification reçu par SMS.

Attention : un message sans faute et avec le bon logo peut être un faux. Les indices techniques (adresse, lien) sont plus fiables que l’apparence.

En résumé : expéditeur, objet, texte, liens, pièces jointes : vérifiez les cinq, et demandez-vous si vous attendiez ce message.

## Analyser un lien
Le lien est l’élément le plus important à vérifier. Une adresse web (URL) se lit de droite à gauche pour trouver le vrai domaine.

Exemple : https://www.mabanque.com/connexion
- Le protocole : https
- Le domaine : mabanque.com
- Le chemin : /connexion

Pour trouver le vrai domaine : isolez ce qui se trouve entre « https:// » et le premier « / », puis gardez en général les deux derniers morceaux séparés par des points (par exemple mabanque.com). Tout ce qui est à gauche est un sous-domaine, que l’attaquant peut choisir librement.

Exemple piégé : https://mabanque.com.securite-verif.xyz/connexion
Ici, le vrai domaine est securite-verif.xyz. Le nom « mabanque.com » n’est qu’un sous-domaine pour tromper l’œil.

Autres astuces d’attaquants :
- Fautes volontaires : paypa1.com (chiffre 1 au lieu de l), rnicrosoft.com (rn à la place de m).
- Caractères d’autres alphabets qui ressemblent aux lettres latines (homoglyphes).
- Mots rassurants dans le domaine : « securite », « verification », « officiel ».
- Liens raccourcis (bit.ly...) qui cachent la destination.
- Extensions inhabituelles pour un service officiel.

Le cadenas du navigateur (HTTPS) indique seulement que la connexion est chiffrée, pas que le site est honnête. Les faux sites ont eux aussi un cadenas.

Bonne pratique : n’utilisez pas le lien du message. Ouvrez votre navigateur et tapez vous-même l’adresse du site, ou utilisez l’application officielle.

En résumé : repérez le vrai domaine, méfiez-vous des fautes d’orthographe dans l’adresse, et tapez l’adresse vous-même.

## L’usurpation d’adresse et les protections techniques
Un attaquant peut faire apparaître n’importe quelle adresse dans le champ « De ». Pour lutter contre cela, les fournisseurs d’e-mail utilisent trois mécanismes.

- SPF : une liste publiée par le domaine indiquant quels serveurs ont le droit d’envoyer des e-mails en son nom.
- DKIM : une signature numérique ajoutée au message, qui prouve qu’il n’a pas été modifié et qu’il vient bien du domaine.
- DMARC : la règle qui dit quoi faire si SPF ou DKIM échouent (accepter, mettre en spam, rejeter).

Quand ces protections sont bien configurées, votre messagerie filtre beaucoup de faux messages. Mais elles ne sont pas parfaites : un attaquant peut utiliser un domaine très proche (typosquatting) avec ses propres configurations valides, ou un vrai compte piraté.

Le filtre anti-spam est une aide, pas une garantie. C’est pourquoi votre vigilance reste indispensable.

Sur le plan de l’organisation, les entreprises sérieuses ajoutent d’autres règles : validation d’un virement par deux personnes, vérification par téléphone avant tout changement de coordonnées bancaires, formation et exercices de phishing internes.

En résumé : SPF, DKIM et DMARC filtrent une partie des faux e-mails, mais un domaine proche ou un compte piraté passe quand même. Ne comptez pas uniquement sur le filtre.

## Trois exemples analysés
Exemple 1 : le faux SMS de livraison
« Votre colis n’a pas pu être livré. Confirmez votre adresse et payez 1,50 € : lien-court.xyz/ab12 »
Indices : lien raccourci, domaine inconnu, petit paiement, pas de nom ni de numéro de suivi. Réaction : ne pas cliquer, supprimer. Si vous attendez vraiment un colis, allez sur le site du transporteur et saisissez le numéro de suivi.

Exemple 2 : le faux e-mail de la banque
Objet : « Activité suspecte sur votre compte, action requise ». Expéditeur : service@mabanque-securite.com. Texte : « Cher client, connectez-vous dans les 24 h via ce lien pour éviter la suspension. »
Indices : salutation générique, urgence, menace, domaine différent du vrai site de la banque. Réaction : ne pas cliquer, ouvrir l’application officielle ou appeler le numéro écrit sur votre carte.

Exemple 3 : le spear phishing
Vous recevez un e-mail de « votre responsable de stage » : « Peux-tu me régler un petit service rapidement ? Je suis en réunion, achète des cartes cadeaux et envoie-moi les codes. » L’adresse affichée est proche de la vraie, mais avec une lettre différente.
Indices : demande inhabituelle, pression, canal étrange (cartes cadeaux), impossibilité de téléphoner. Réaction : ne rien acheter, appeler la personne sur son numéro connu.

Dans les trois cas, la solution est la même : ne pas réagir dans l’urgence, et vérifier par un canal officiel que vous choisissez vous-même.

En résumé : urgence, lien étrange, demande inhabituelle : trois signaux qui suffisent pour s’arrêter.

## Si vous avez déjà cliqué ou répondu
Cela peut arriver à tout le monde. L’important est de réagir vite et calmement.

Si vous avez cliqué sur un lien mais rien saisi :
- Fermez la page. Si le lien a déclenché un téléchargement, ne l’ouvrez pas et supprimez-le.
- Lancez une analyse antivirus.

Si vous avez saisi un mot de passe :
1. Changez-le tout de suite depuis le site officiel (pas depuis le lien).
2. Changez-le aussi partout où vous utilisiez le même.
3. Activez la double authentification.
4. Déconnectez les autres sessions ouvertes.
5. Regardez l’activité récente du compte.

Si vous avez saisi des données bancaires :
- Contactez immédiatement votre banque pour bloquer la carte et contester les opérations.

Si vous avez ouvert une pièce jointe suspecte :
- Déconnectez l’appareil du réseau, lancez une analyse antivirus complète, et demandez de l’aide.

Dans tous les cas :
- Prévenez les personnes concernées (collègues, amis) si votre compte a pu envoyer des messages à votre place.
- Signalez le message : à votre messagerie, à votre établissement, et en Tunisie à l’agence nationale de la sécurité informatique (ANSI), dont le site officiel indique comment signaler un incident.
- Ne soyez pas honteux : plus on parle vite, plus on limite les dégâts.

En résumé : changer les mots de passe, prévenir la banque si besoin, signaler, et ne jamais attendre par honte.

## Se protéger au quotidien
Des habitudes simples qui réduisent fortement le risque.

- Ne cliquez pas : allez directement sur le site en tapant l’adresse ou via l’application officielle.
- Vérifiez par un autre canal : appelez le numéro officiel, pas celui du message.
- Activez la double authentification : même si votre mot de passe est volé, il ne suffit plus.
- Utilisez un gestionnaire de mots de passe : il ne remplit les champs que sur le vrai site, ce qui aide à repérer un faux.
- Tenez à jour votre navigateur, votre système, votre antivirus.
- Désactivez l’exécution automatique des macros dans les documents.
- Limitez ce que vous publiez : plus un attaquant vous connaît, plus son spear phishing est crédible.
- Signalez les messages suspects (bouton « signaler » de votre messagerie).
- Créez une culture de questions : demander « est-ce vrai ? » à un collègue ou un ami n’est pas ridicule.

Règle d’or : un message qui vous pousse à agir vite, avec un lien ou une pièce jointe, mérite une pause de dix secondes. Ces dix secondes arrêtent la majorité des attaques.

En résumé : aller sur le site par soi-même, vérifier ailleurs, double authentification et pause avant d’agir.

## Mise en pratique et auto-évaluation
Exercice : pour chaque message, dites s’il est probablement légitime ou frauduleux et pourquoi.

Message A : un e-mail de votre université, envoyé depuis une adresse du domaine officiel, qui annonce la date des examens et ne contient aucun lien.
Réponse : probablement légitime. Pas de demande d’action, pas de lien, domaine officiel. Vous pouvez quand même vérifier sur le site de l’établissement.

Message B : un SMS « Votre compte bancaire sera bloqué. Cliquez ici pour le réactiver : banq-secure.top/login ».
Réponse : frauduleux. Urgence, menace, domaine inconnu, banque jamais sérieuse par SMS avec ce type de lien.

Message C : un ami vous écrit sur un réseau social : « Regarde cette vidéo de toi, c’est incroyable ! » avec un lien.
Réponse : à ne pas ouvrir. Le compte de votre ami est peut-être piraté. Contactez-le par un autre moyen pour vérifier.

Q1. Dans https://compte.paypal.com.verification-securite.net/login, quel est le vrai domaine ?
Réponse : verification-securite.net. Le reste est un sous-domaine choisi par l’attaquant.

Q2. Un site a un cadenas HTTPS. Est-il forcément sûr ?
Réponse : non. Le cadenas signifie que la connexion est chiffrée, pas que le site est honnête.

Q3. Quelle est la meilleure façon de vérifier un message de votre banque ?
Réponse : ne pas utiliser ses liens, ouvrir l’application officielle ou appeler le numéro inscrit sur votre carte.

Q4. Qu’est-ce que le spear phishing ?
Réponse : un phishing ciblé, personnalisé avec des informations réelles sur la victime.

Q5. Vous avez saisi votre mot de passe sur un faux site. Quelles sont les deux premières actions ?
Réponse : le changer immédiatement sur le vrai site (et partout où il est réutilisé), puis activer la double authentification.

Vous pouvez maintenant vous entraîner avec la simulation Phishing Detector.',
2)
ON CONFLICT (id) DO UPDATE SET
    titre = EXCLUDED.titre,
    description = EXCLUDED.description,
    contenu = EXCLUDED.contenu,
    ordre = EXCLUDED.ordre;


INSERT INTO Courses (id, titre, description, contenu, ordre)
OVERRIDING SYSTEM VALUE VALUES (
3,
'Malware',
'Virus, vers, chevaux de Troie, ransomware : comment ils agissent et comment s’en protéger.',
'## Qu’est-ce qu’un malware ?
Un malware (de « malicious software », logiciel malveillant) est un programme conçu pour agir contre l’intérêt de son utilisateur : voler des données, espionner, détruire ou chiffrer des fichiers, utiliser votre appareil pour attaquer d’autres personnes, ou afficher des publicités de force.

Les motivations des créateurs de malwares sont variées :
- L’argent (rançons, vol de données bancaires, minage de cryptomonnaie à vos frais).
- L’espionnage (surveillance d’une personne, d’une entreprise, d’un État).
- Le sabotage (détruire ou bloquer un système).
- La revente d’accès : un appareil infecté peut être loué à d’autres criminels.

Un malware n’est pas toujours spectaculaire. Beaucoup se cachent et agissent silencieusement pendant des mois. Une machine infectée peut sembler tout à fait normale.

Le mot « virus » est souvent utilisé à tort pour parler de tous les malwares. En réalité, le virus n’est qu’une famille parmi d’autres, que vous allez découvrir dans la partie suivante.

En résumé : un malware est un logiciel malveillant, et il peut agir discrètement très longtemps.

## Les grandes familles de malwares
Chaque type agit différemment.

- Virus : s’attache à un programme ou à un fichier et se propage quand l’utilisateur l’exécute. Il a besoin d’une action humaine.
- Ver (worm) : se copie tout seul d’un appareil à l’autre à travers le réseau, sans intervention humaine. Il peut contaminer très vite.
- Cheval de Troie (trojan) : se présente comme un logiciel utile ou un jeu mais exécute une action cachée. Il ne se copie pas tout seul, c’est l’utilisateur qui l’installe.
- Ransomware (rançongiciel) : chiffre vos fichiers et exige un paiement pour les débloquer.
- Spyware (logiciel espion) : observe votre activité et envoie les informations à l’attaquant.
- Keylogger : enregistre ce que vous tapez au clavier, donc vos mots de passe.
- Adware : affiche des publicités intrusives, parfois en collectant des données.
- Rootkit : se cache profondément dans le système pour rester invisible.
- Backdoor (porte dérobée) : laisse à l’attaquant un accès à distance.
- Botnet : réseau d’appareils infectés contrôlés à distance, utilisés pour envoyer du spam ou saturer des sites.
- Cryptojacking : utilise la puissance de votre machine pour miner de la cryptomonnaie. Symptôme : lenteur et surchauffe.

Un malware réel combine souvent plusieurs de ces comportements. Exemple : un trojan qui installe un keylogger puis ouvre une porte dérobée.

En résumé : virus, ver, trojan, ransomware, spyware : chacun a son mode de propagation et son effet.

## Comment un malware arrive-t-il chez vous ?
Un malware doit d’abord entrer. Voici les chemins les plus courants, qu’on appelle vecteurs d’infection.

1. Les e-mails de phishing : une pièce jointe piégée ou un lien qui télécharge le malware.
2. Les téléchargements : logiciels piratés, cracks, faux installateurs, fausses mises à jour.
3. Les sites compromis ou les fausses publicités qui déclenchent un téléchargement.
4. Les clés USB et disques externes inconnus ou prêtés.
5. Les failles non corrigées : un système pas à jour peut être infecté simplement en étant connecté au réseau.
6. Les applications mobiles : fausses applications hors des boutiques officielles, ou même des applications qui demandent des permissions excessives.
7. Les macros dans les documents Office : le document demande d’« activer le contenu » pour afficher quelque chose.
8. Les faux supports techniques : une fenêtre qui affirme « Votre ordinateur est infecté, appelez ce numéro ».

Point clé : dans la majorité des cas, le malware compte sur une action de l’utilisateur (un clic, un téléchargement, une autorisation). C’est pourquoi la prévention passe autant par les comportements que par les outils.

Les logiciels piratés sont un cas à part : la raison pour laquelle un logiciel payant est gratuit, c’est souvent que quelqu’un a ajouté quelque chose dedans.

En résumé : e-mails, téléchargements douteux, clés USB, failles non corrigées : presque toujours, un clic ouvre la porte.

## Cas réel : WannaCry
En mai 2017, le ransomware WannaCry a infecté en quelques jours plus de 150 pays et des centaines de milliers d’ordinateurs. Des hôpitaux au Royaume-Uni ont dû annuler des rendez-vous et détourner des ambulances.

Comment il fonctionnait :
- C’était un ver : il se propageait seul d’un ordinateur à l’autre dans le réseau, sans qu’il faille cliquer sur quoi que ce soit.
- Il exploitait une vulnérabilité du protocole de partage de fichiers de Windows (SMB).
- Il chiffrait les fichiers et affichait un message demandant une rançon en bitcoins.

Ce que cela nous apprend :
1. Microsoft avait publié le correctif plusieurs semaines avant l’attaque. Les ordinateurs mis à jour n’ont pas été touchés. Les mises à jour sont une protection majeure.
2. Les systèmes anciens, qui n’étaient plus pris en charge, étaient particulièrement exposés.
3. Un seul ordinateur infecté dans un réseau pouvait contaminer tous les autres : la segmentation du réseau aurait limité les dégâts.
4. Les sauvegardes hors ligne auraient permis de restaurer les données sans payer.

La leçon : une protection simple (la mise à jour) aurait évité la plupart des dégâts. Les attaquants ne cherchent pas la sophistication, ils cherchent les machines non corrigées.

En résumé : WannaCry a prouvé que les mises à jour et les sauvegardes sont plus importantes que beaucoup d’outils complexes.

## Le ransomware en détail
Le ransomware est aujourd’hui l’un des malwares les plus coûteux. Il se déroule en plusieurs étapes.

1. Intrusion : phishing, mot de passe volé, faille non corrigée.
2. Exploration : l’attaquant observe le réseau, cherche les sauvegardes et les données importantes.
3. Vol : il copie souvent les données avant de chiffrer, pour avoir un second moyen de pression (double extorsion).
4. Chiffrement : les fichiers deviennent illisibles sans la clé.
5. Demande de rançon : un message explique comment payer, souvent en cryptomonnaie, avec un compte à rebours.

Faut-il payer ? Les spécialistes déconseillent de payer, pour plusieurs raisons :
- Rien ne garantit que les fichiers seront rendus, ou entièrement.
- Le paiement finance d’autres attaques.
- La victime est parfois ciblée de nouveau, car elle a prouvé qu’elle paie.
- Si les données ont été volées, elles peuvent être publiées malgré le paiement.

La meilleure défense est préventive : sauvegardes testées et hors ligne, mises à jour, double authentification, sensibilisation au phishing. Si vous avez des sauvegardes récentes, un ransomware devient un incident gênant, pas une catastrophe.

La règle de sauvegarde 3-2-1 : 3 copies de vos données, sur 2 supports différents, dont 1 copie hors de chez vous ou hors ligne.

Si vous êtes touché : débranchez la machine du réseau, ne payez pas dans la précipitation, prévenez les responsables, et cherchez de l’aide (certains ransomwares ont des outils de déchiffrement publics).

En résumé : le ransomware chiffre et souvent vole. La vraie protection, ce sont les sauvegardes 3-2-1 et les mises à jour.

## Reconnaître une infection
Les signes ne sont pas toujours visibles, mais voici des indices fréquents.

Sur un ordinateur :
- Lenteur soudaine, ventilateur qui tourne fort sans raison.
- Fenêtres publicitaires, pages d’accueil du navigateur qui changent.
- Programmes que vous n’avez pas installés, extensions de navigateur inconnues.
- Antivirus désactivé sans votre action.
- Fichiers qui changent d’extension ou deviennent illisibles.
- Messages envoyés depuis vos comptes à votre insu.
- Connexion Internet utilisée alors que vous ne faites rien.

Sur un téléphone :
- Batterie qui se vide très vite, appareil qui chauffe.
- Applications inconnues, forfait de données consommé anormalement.
- Publicités qui apparaissent hors des applications.
- Appels ou SMS que vous n’avez pas envoyés.

Attention aux faux signes : un message du navigateur qui affirme « 5 virus détectés, appelez ce numéro » est lui-même une arnaque (faux support technique), pas une vraie détection.

Que faire si vous avez un doute : lancez une analyse complète avec un antivirus fiable, vérifiez la liste des programmes et des extensions installés, et surveillez vos comptes. En cas de doute sérieux, faites-vous aider ou réinitialisez l’appareil.

En résumé : lenteur anormale, programmes inconnus, publicités et comportements étranges sont des signes, mais certains malwares sont totalement discrets.

## Comment fonctionne un antivirus
Un antivirus (ou solution de sécurité) combine plusieurs méthodes de détection.

1. Détection par signature : le logiciel compare les fichiers à une base de signatures de malwares connus. Efficace contre l’existant, inutile contre un malware nouveau.
2. Détection heuristique : il cherche des caractéristiques suspectes dans le code, même si le programme est inconnu.
3. Détection comportementale : il surveille ce que fait un programme (chiffrer beaucoup de fichiers rapidement, modifier des paramètres système, se connecter à un serveur suspect) et bloque ce qui ressemble à une attaque.
4. Sandbox : le programme suspect est exécuté dans un environnement isolé pour observer son comportement.
5. Protection web et e-mail : blocage des sites et pièces jointes connus comme dangereux.

Limites : aucun antivirus ne détecte tout. Les nouveaux malwares ou les attaques ciblées peuvent passer. Un antivirus n’est qu’une couche de la défense, il ne remplace ni les mises à jour ni la prudence.

Bonnes pratiques :
- Gardez l’antivirus actif et à jour.
- Sur Windows, l’antivirus intégré (Microsoft Defender) est un choix sérieux pour la plupart des utilisateurs.
- N’installez pas plusieurs antivirus à la fois : ils se gênent.
- Méfiez-vous des faux antivirus, qui sont eux-mêmes des malwares.

En résumé : l’antivirus combine signatures, heuristique et comportement, mais ne remplace ni les mises à jour ni la prudence.

## Se protéger : la checklist
Voici une liste complète de protections, par ordre d’efficacité.

1. Mettre à jour : activez les mises à jour automatiques du système, du navigateur et des applications.
2. Sauvegarder : appliquez la règle 3-2-1 et testez de temps en temps une restauration.
3. Installer depuis les sources officielles : boutiques officielles ou sites des éditeurs. Évitez les logiciels piratés.
4. Ne pas travailler en administrateur : utilisez un compte standard pour l’usage quotidien (moindre privilège). Un malware aura moins de pouvoir.
5. Désactiver les macros Office par défaut, et ne jamais cliquer sur « Activer le contenu » pour un document inattendu.
6. Se méfier des pièces jointes et des liens, même d’un contact connu : son compte peut être piraté.
7. Activer le pare-feu et l’antivirus.
8. Ne pas brancher de clés USB inconnues.
9. Afficher les extensions de fichiers : « facture.pdf.exe » est un programme, pas un PDF.
10. Chiffrer l’appareil et verrouiller l’écran pour protéger les données en cas de vol.
11. Désinstaller ce qu’on n’utilise plus : moins de programmes, moins de failles.

Chaque mesure est simple. Prises ensemble, elles bloquent la grande majorité des infections.

En résumé : mises à jour, sauvegardes, sources officielles, compte standard et prudence avec les pièces jointes.

## Réagir en cas d’infection
Si vous pensez qu’un appareil est infecté, suivez ces étapes dans l’ordre.

1. Isoler : déconnectez l’appareil d’Internet (Wi-Fi, câble) pour stopper la fuite de données et éviter la propagation.
2. Ne pas éteindre dans la panique si un ransomware est en cours : demandez conseil. Éteindre peut parfois aider à limiter le chiffrement, mais les avis varient selon le cas.
3. Changer les mots de passe depuis un autre appareil sain : e-mail, banque, réseaux sociaux. Activez la double authentification.
4. Analyser : lancez une analyse antivirus complète, éventuellement avec un second outil de vérification fiable.
5. Nettoyer ou réinstaller : si l’infection est grave ou persistante, la solution la plus sûre est de réinstaller le système.
6. Restaurer vos données depuis une sauvegarde saine (vérifiée avant de la rebrancher).
7. Prévenir : les personnes concernées, votre établissement ou employeur, et votre banque si des données de paiement ont pu être volées.
8. Comprendre : comment l’infection est arrivée, pour corriger la cause (mise à jour manquante, mauvais clic, logiciel douteux).

Les erreurs fréquentes à éviter : payer une rançon sans réfléchir, rebrancher une sauvegarde sur un système encore infecté, ignorer le problème en espérant qu’il disparaisse.

En résumé : isoler, changer les mots de passe, nettoyer ou réinstaller, restaurer, prévenir et comprendre.

## Les malwares sur téléphone
Les smartphones aussi sont ciblés, surtout Android avec des applications installées hors des boutiques officielles.

Les risques :
- Fausses applications (jeux, outils, applications « gratuites » de services payants).
- Applications qui demandent des permissions sans rapport : une lampe de poche qui veut accéder à vos contacts et SMS.
- Faux SMS contenant un lien qui installe une application piégée.
- Applications de surveillance installées en secret sur le téléphone d’une personne (stalkerware).

Les bons réflexes :
- Installer uniquement depuis les boutiques officielles, et lire les avis et le nom du développeur.
- Vérifier les permissions demandées. Refusez ce qui n’est pas nécessaire.
- Ne pas autoriser l’« installation depuis des sources inconnues » sauf cas précis.
- Mettre à jour le système et les applications.
- Désinstaller les applications inutilisées.
- Verrouiller le téléphone avec un code solide et activer la localisation à distance.
- Se méfier des SMS et des messages avec des liens.

En cas de doute : regardez dans les paramètres les applications qui consomment beaucoup de données ou de batterie, et celles qui ont des droits d’administrateur.

En résumé : sur mobile, boutiques officielles, permissions limitées et mises à jour.

## À retenir et auto-évaluation
Ce qu’il faut retenir :
- Un malware est un logiciel malveillant : virus, ver, trojan, ransomware, spyware, keylogger, botnet...
- La plupart des infections commencent par une action de l’utilisateur ou par une faille non corrigée.
- WannaCry a montré le poids des mises à jour et des sauvegardes.
- Règle 3-2-1 : 3 copies, 2 supports, 1 hors site ou hors ligne.
- Un antivirus aide mais ne remplace pas la prudence.
- En cas d’infection : isoler, changer les mots de passe, nettoyer, restaurer, prévenir.

Testez-vous :

Q1. Quelle est la différence entre un virus et un ver ?
Réponse : un virus a besoin qu’on exécute le fichier infecté. Un ver se propage seul à travers le réseau.

Q2. Pourquoi ne faut-il pas payer la rançon d’un ransomware ?
Réponse : aucune garantie de récupération, cela finance d’autres attaques, et les données volées peuvent être publiées malgré tout.

Q3. Expliquez la règle de sauvegarde 3-2-1.
Réponse : 3 copies des données, sur 2 supports différents, dont 1 copie hors site ou hors ligne.

Q4. Un document inattendu demande « Activer le contenu ». Que faire ?
Réponse : ne pas activer. Les macros servent souvent à installer un malware. Vérifiez avec l’expéditeur par un autre canal.

Q5. Pourquoi utiliser un compte standard plutôt qu’administrateur au quotidien ?
Réponse : c’est le moindre privilège : un malware lancé depuis un compte standard a moins de pouvoir pour modifier le système.

Q6. Un message du navigateur affirme « 5 virus détectés, appelez ce numéro ». Est-ce fiable ?
Réponse : non, c’est une arnaque de faux support technique.

Vous avez terminé le cours. Le cours suivant explique comment les attaquants manipulent les personnes.',
3)
ON CONFLICT (id) DO UPDATE SET
    titre = EXCLUDED.titre,
    description = EXCLUDED.description,
    contenu = EXCLUDED.contenu,
    ordre = EXCLUDED.ordre;


INSERT INTO Courses (id, titre, description, contenu, ordre)
OVERRIDING SYSTEM VALUE VALUES (
4,
'Social Engineering',
'Comment les attaquants manipulent les personnes, et comment leur résister.',
'## Qu’est-ce que l’ingénierie sociale ?
L’ingénierie sociale (social engineering) regroupe les techniques qui exploitent la psychologie humaine plutôt que des failles techniques. L’attaquant ne force pas la porte : il convainc quelqu’un de l’ouvrir.

Le but peut être :
- Obtenir une information (mot de passe, code, numéro de carte, organisation interne).
- Obtenir une action (cliquer, installer, virer de l’argent, ouvrir une porte).
- Obtenir un accès physique (entrer dans un bâtiment, brancher un appareil).

Le phishing que vous avez étudié est une forme d’ingénierie sociale par message. Elle existe aussi au téléphone, en face à face, sur les réseaux sociaux et par messagerie.

Un principe de base : il est souvent plus facile et moins coûteux de tromper une personne que de casser un système bien protégé. Un chiffrement très solide ne sert à rien si quelqu’un donne le mot de passe à un faux technicien.

Exemple : une personne appelle un étudiant en disant être du service informatique de la faculté et demande son mot de passe « pour corriger un problème sur son compte ». Aucun système n’a été attaqué : la victime a elle-même livré la clé.

En résumé : l’ingénierie sociale attaque la personne, pas la machine, car c’est souvent le chemin le plus facile.

## Pourquoi nous sommes vulnérables
Ces attaques fonctionnent parce qu’elles s’appuient sur des réflexes normaux et utiles dans la vie sociale.

- Nous voulons aider : refuser de l’aide à quelqu’un qui semble en difficulté nous met mal à l’aise.
- Nous faisons confiance : à une voix aimable, à une personne en uniforme, à quelqu’un qui connaît des détails sur nous.
- Nous respectons l’autorité : on obéit plus facilement à quelqu’un qui paraît important.
- Nous évitons les conflits : dire non, demander une preuve, paraît impoli.
- Nous réagissons vite sous pression : l’urgence réduit la réflexion.
- Nous sommes distraits, fatigués, surchargés.

Les attaquants le savent et fabriquent des situations où le bon réflexe social est le mauvais réflexe de sécurité.

Une idée à garder en tête : se faire manipuler n’est pas un signe de bêtise. Ces techniques sont étudiées, répétées et très efficaces, y compris contre des professionnels. Le but de ce cours n’est pas de vous rendre méfiant envers tout le monde, mais de vous donner des habitudes de vérification simples qui ne dépendent pas de votre état du moment.

En résumé : l’ingénierie sociale détourne des réflexes sociaux normaux : aider, faire confiance, obéir, aller vite.

## Les leviers de persuasion
Le psychologue Robert Cialdini a décrit des principes de persuasion que les attaquants reprennent presque tous.

1. Réciprocité : on se sent obligé de rendre un service. Exemple : un « technicien » vous aide gentiment, puis demande votre mot de passe.
2. Engagement et cohérence : après avoir dit oui à une petite demande, on accepte plus facilement la suivante. L’attaquant commence par des questions anodines.
3. Preuve sociale : « tout le monde l’a déjà fait ». Exemple : « Vos collègues ont tous installé ce correctif. »
4. Autorité : on obéit à une figure d’autorité. Exemple : un faux directeur, un faux policier, un faux agent de banque.
5. Sympathie : on dit plus facilement oui à quelqu’un qu’on apprécie, qui nous ressemble, qui nous flatte.
6. Rareté et urgence : « offre limitée », « dernière chance », « compte bloqué dans 1 heure ».

En plus de ces six, on retrouve souvent la peur (menace de sanction), la curiosité et l’appât du gain.

Comment s’en servir pour vous protéger : quand vous sentez une pression forte de type urgence, autorité, flatterie ou obligation, arrêtez-vous et demandez-vous : pourquoi cette personne a-t-elle besoin que j’agisse maintenant, et puis-je vérifier ce qu’elle me dit autrement ?

En résumé : urgence, autorité, sympathie, preuve sociale, réciprocité : reconnaître ces leviers permet de les désamorcer.

## Le déroulement d’une attaque
Une attaque d’ingénierie sociale suit souvent quatre étapes.

1. Recherche : l’attaquant collecte des informations sur la cible, surtout sur Internet. On appelle cela l’OSINT (renseignement en sources ouvertes). Il regarde les réseaux sociaux, les profils professionnels, le site de l’organisation, les photos, les offres d’emploi.
2. Prise de contact : il crée une relation ou une situation crédible (faux profil, faux appel, faux e-mail), en utilisant des détails réels pour inspirer confiance.
3. Exploitation : il demande l’information ou l’action. La demande paraît souvent logique grâce au contexte construit avant.
4. Sortie : il disparaît ou continue sans éveiller de soupçon, parfois en revenant plus tard avec une demande plus grosse.

Exemple : l’attaquant repère sur un réseau social qu’une étudiante fait un stage dans une entreprise et qui est son responsable. Il envoie un e-mail à l’étudiante en se faisant passer pour ce responsable et demande un document « urgent ». Tout est crédible parce que les noms sont réels.

Remarque : les attaques sérieuses prennent du temps. Un contact qui demande très peu au début puis de plus en plus est un signal d’alerte.

En résumé : recherche, contact, exploitation, sortie : plus vous publiez, plus l’étape 1 est facile pour l’attaquant.

## Les techniques courantes
Voici les techniques que vous devez savoir reconnaître.

- Pretexting : l’attaquant invente un scénario crédible (une fausse identité, un faux motif) pour obtenir des informations. Exemple : un faux « sondage » ou un faux support technique.
- Phishing, smishing, vishing : messages, SMS et appels frauduleux.
- Baiting (appât) : on laisse un objet attirant, comme une clé USB étiquetée « Salaires 2025 », dans un couloir ou un parking. La curiosité pousse à la brancher, et elle contient un malware.
- Quid pro quo : « je vous aide en échange d’une information ». Exemple : un faux technicien propose de régler un problème si vous lui donnez accès à votre ordinateur.
- Tailgating (ou piggybacking) : suivre une personne autorisée pour entrer dans un lieu protégé. « Pouvez-vous tenir la porte, j’ai les mains pleines ? »
- Shoulder surfing : regarder par-dessus l’épaule pour voir un code ou un mot de passe, dans un café, un transport, un distributeur.
- Dumpster diving : fouiller les poubelles pour trouver des documents jetés sans être détruits.
- Faux support technique : appel ou fenêtre qui prétend que votre ordinateur est infecté.
- Usurpation de voix ou de visage par intelligence artificielle (deepfake) : une voix clonée peut imiter un proche ou un responsable. Cela rend la vérification par un second canal encore plus importante.
- Fraude au président : faux ordre de virement émanant de la direction.

La plupart de ces techniques demandent peu de moyens techniques.

En résumé : pretexting, baiting, tailgating, faux support et voix clonées : toujours la même idée, une histoire crédible pour vous faire agir.

## Cas réel : l’attaque de Twitter en 2020
En juillet 2020, des comptes très connus de la plateforme Twitter (aujourd’hui X) ont été piratés pour publier une arnaque à la cryptomonnaie. L’attaque n’a pas exploité une faille technique complexe.

Ce qui s’est passé, en résumé :
- Les attaquants ont téléphoné à des employés de l’entreprise en se faisant passer pour des collègues du support informatique (vishing).
- Ils ont convaincu certains employés de donner leurs identifiants et d’utiliser un faux site de connexion.
- Avec ces accès, ils ont pu utiliser les outils internes de l’entreprise pour prendre le contrôle de comptes de personnalités.

Ce que cela nous apprend :
1. Même une grande entreprise technologique est vulnérable à une simple conversation téléphonique bien menée.
2. Un accès interne puissant (les outils d’administration) multiplie l’impact : un seul employé trompé suffit.
3. La double authentification classique ne suffit pas toujours si l’employé donne lui-même les codes sur un faux site. Les clés de sécurité physiques résistent mieux à ce type d’attaque.
4. Les procédures de vérification (rappeler un numéro officiel, valider auprès d’un responsable) et la limitation des droits (moindre privilège) auraient réduit les dégâts.

La leçon : la sécurité d’une organisation dépend de ses procédures et de la formation de ses personnes autant que de sa technologie.

En résumé : un simple appel a suffi à piéger des employés d’une grande entreprise, d’où l’importance de vérifier toute demande inhabituelle.

## Ce que vous publiez nourrit l’attaquant
Plus vous partagez en public, plus il est facile de vous cibler. Voici ce qui aide un attaquant sans que vous vous en rendiez compte.

- Votre nom complet, votre ville, votre établissement, votre entreprise, votre poste.
- Les noms de vos proches, de vos animaux, la date de naissance : ce sont les réponses aux questions de sécurité et la base de beaucoup de mots de passe.
- Vos photos : un badge, un écran, un billet, une carte visibles, ou la façade de chez vous.
- Vos déplacements et vos vacances : une maison vide ou un compte sans surveillance.
- Vos contacts et vos groupes : pour se faire passer pour un ami.
- Vos messages professionnels : tournures, noms de projets, outils utilisés.

Que faire :
1. Passez vos comptes en privé et vérifiez régulièrement les paramètres de confidentialité.
2. Ne publiez pas de documents officiels, de billets, de cartes, de badges.
3. Évitez de publier votre emploi du temps et vos vacances en direct.
4. Acceptez uniquement les personnes que vous connaissez réellement.
5. Utilisez des réponses de sécurité fausses mais mémorisables, ou mieux, la double authentification.
6. Cherchez votre propre nom sur un moteur de recherche : vous verrez ce qu’un attaquant verrait.

L’objectif n’est pas de disparaître d’Internet, mais de décider consciemment ce qui est public.

En résumé : tout ce qui est public peut servir à une attaque personnalisée. Réduisez la surface visible.

## Se protéger : vérifier, ralentir, signaler
Trois habitudes simples défendent contre presque toutes les attaques d’ingénierie sociale.

1. Ralentir
Une demande urgente est un signal d’alerte, pas une raison d’obéir. Prenez le temps de réfléchir. Une vraie urgence supporte une vérification de cinq minutes.

2. Vérifier par un autre canal
Ne vous fiez pas aux coordonnées données par la personne qui vous contacte. Rappelez le numéro officiel que vous connaissez, écrivez à l’adresse habituelle, parlez-en à un collègue ou un responsable. Pour un proche qui demande de l’argent par message ou appel, posez une question dont seule la vraie personne connaît la réponse, ou convenez d’un mot de passe familial.

3. Appliquer des règles qui ne dépendent pas de la personne
- Ne jamais donner un mot de passe ou un code de vérification : un service sérieux ne le demande pas.
- Ne jamais installer un logiciel ou donner un accès à distance à quelqu’un qui vous a contacté.
- Ne jamais brancher un appareil inconnu.
- Ne pas faire entrer quelqu’un dans un lieu protégé sans qu’il s’identifie, même poliment.
- Pour tout paiement inhabituel, demander une validation indépendante.

Et signalez : prévenez votre responsable, votre établissement ou le service concerné, même si l’attaque a échoué. Un signalement permet d’avertir les autres.

Il est normal de dire non poliment : « Je préfère vérifier et je vous rappelle. » Un interlocuteur honnête comprendra.

En résumé : ralentir, vérifier par un autre canal, appliquer des règles fixes, et signaler.

## Scénarios à analyser
Pour chaque scénario, réfléchissez avant de lire la réponse.

Scénario 1
Un appel : « Bonjour, ici le support de votre fournisseur Internet. Nous avons détecté un virus sur votre connexion. Installez cette application pour que je prenne le contrôle de votre ordinateur et je corrige. »
Analyse : appel non sollicité, urgence, demande d’installer un outil de prise de contrôle à distance. Réaction : raccrocher. Les fournisseurs ne font pas cela. Si vous voulez vérifier, appelez le service client avec le numéro officiel.

Scénario 2
Quelqu’un en tenue professionnelle attend devant la porte d’un bâtiment avec un carton dans les mains : « Vous pouvez m’ouvrir, j’ai oublié mon badge ? »
Analyse : tailgating. Réaction : l’inviter à passer par l’accueil ou à utiliser son badge, poliment. Une personne légitime comprendra.

Scénario 3
Un message d’un ami sur un réseau social : « Salut, je suis bloqué à l’étranger, peux-tu m’envoyer 200 euros par transfert ? Je te rembourse. »
Analyse : probablement un compte piraté ou un faux profil. Réaction : appeler l’ami sur son numéro connu pour vérifier avant tout envoi.

Scénario 4
Vous trouvez une clé USB devant l’université avec l’étiquette « Résultats examens ».
Analyse : baiting. Réaction : ne pas la brancher. La remettre à l’administration ou à un responsable informatique.

Scénario 5
Un e-mail de la « direction » : « Je suis en déplacement, je ne peux pas t’appeler. Fais un virement urgent à ce compte pour un fournisseur. »
Analyse : fraude au président. Réaction : ne rien faire avant d’avoir vérifié par un autre canal et suivi la procédure de validation.

Pour chaque cas : l’attaquant crée de l’urgence, joue sur un levier (autorité, aide, curiosité) et empêche la vérification. Votre réflexe doit être de vérifier avant d’agir.

En résumé : urgence, levier émotionnel, impossibilité de vérifier : trois signes à chercher à chaque fois.

## À retenir et auto-évaluation
Ce qu’il faut retenir :
- L’ingénierie sociale attaque les personnes, pas les machines.
- Les attaquants utilisent des leviers : urgence, autorité, sympathie, peur, curiosité, preuve sociale, réciprocité.
- Une attaque suit des étapes : recherche, contact, exploitation, sortie.
- Ce que vous publiez en ligne aide les attaquants à vous cibler.
- Trois défenses : ralentir, vérifier par un autre canal, signaler.

Testez-vous :

Q1. Quelle est la différence entre le vishing et le smishing ?
Réponse : le vishing se fait par appel téléphonique, le smishing par SMS ou messagerie.

Q2. Qu’est-ce que le tailgating ?
Réponse : suivre une personne autorisée pour entrer dans un lieu protégé sans avoir soi-même le droit d’accès.

Q3. Pourquoi l’urgence est-elle un signal d’alerte ?
Réponse : elle réduit la réflexion et empêche la vérification. C’est un levier classique des attaquants.

Q4. Quelqu’un qui dit être du support technique vous appelle et vous demande un code reçu par SMS. Que faites-vous ?
Réponse : je refuse, je raccroche, et je contacte le service par son numéro officiel. Un code de vérification ne se donne jamais.

Q5. Nommez deux informations publiques qui peuvent aider un attaquant à vous cibler.
Réponse : par exemple votre établissement ou entreprise, vos proches, vos déplacements, vos photos avec un badge visible.

Q6. Qu’a montré l’attaque contre Twitter en 2020 ?
Réponse : qu’une simple conversation téléphonique peut tromper des employés d’une grande entreprise, et que les procédures de vérification et le moindre privilège sont essentiels.

Vous avez terminé le cours. Le suivant porte sur la sécurité des réseaux.',
4)
ON CONFLICT (id) DO UPDATE SET
    titre = EXCLUDED.titre,
    description = EXCLUDED.description,
    contenu = EXCLUDED.contenu,
    ordre = EXCLUDED.ordre;


INSERT INTO Courses (id, titre, description, contenu, ordre)
OVERRIDING SYSTEM VALUE VALUES (
5,
'Network Security',
'Comprendre les réseaux, leurs menaces, et les protéger avec pare-feu, VPN et chiffrement.',
'## Comment fonctionne un réseau
Pour protéger un réseau, il faut d’abord comprendre comment il fonctionne, au moins dans les grandes lignes.

Un réseau relie des appareils (ordinateurs, téléphones, imprimantes, caméras) pour qu’ils échangent des données.

Les éléments à connaître :
- Adresse IP : l’adresse d’un appareil sur un réseau, comme l’adresse postale d’une maison. À la maison, vos appareils ont des adresses privées (par exemple 192.168.1.20) et partagent une adresse publique vers Internet.
- Routeur (ou box) : l’appareil qui relie votre réseau local à Internet et dirige les données.
- Paquets : les données ne voyagent pas d’un bloc, elles sont découpées en petits paquets qui sont réassemblés à l’arrivée.
- Ports : chaque service utilise un numéro de port, comme une porte numérotée dans un immeuble. Par exemple 80 pour le web non chiffré (HTTP), 443 pour le web chiffré (HTTPS), 22 pour l’accès à distance sécurisé (SSH), 53 pour le DNS.
- DNS : l’annuaire d’Internet. Il transforme un nom comme exemple.com en adresse IP.
- Wi-Fi : un réseau sans fil. Les données voyagent par ondes, donc potentiellement à portée de n’importe qui.
- Serveur et client : le client (votre navigateur) demande, le serveur (le site) répond.

Ce que cela implique pour la sécurité : à chaque étape (votre appareil, le Wi-Fi, le routeur, Internet, le serveur), des données passent et peuvent être attaquées. Il y a donc plusieurs endroits à protéger.

En résumé : adresses IP, routeur, paquets, ports et DNS : chaque élément est un point où l’on peut sécuriser ou attaquer.

## Les menaces sur les réseaux
Voici les attaques réseau les plus courantes, présentées pour que vous sachiez les reconnaître et vous en défendre.

- Écoute du trafic (sniffing) : intercepter les données qui circulent, surtout sur un Wi-Fi ouvert ou si la connexion n’est pas chiffrée. Contre-mesure : chiffrement (HTTPS, VPN, WPA2/WPA3).
- Attaque de l’homme du milieu (man-in-the-middle) : l’attaquant s’interpose entre vous et le service pour lire ou modifier les échanges. Contre-mesure : HTTPS, vérification des alertes de certificat, éviter les réseaux douteux.
- Faux point d’accès Wi-Fi (evil twin) : un réseau qui porte le nom d’un Wi-Fi connu (« Café_Gratuit ») mais appartient à l’attaquant. Contre-mesure : vérifier le nom exact auprès du lieu, utiliser un VPN, ne pas faire d’opérations sensibles.
- Usurpation DNS : fausses réponses DNS qui vous envoient sur un faux site. Contre-mesure : DNS sécurisé, HTTPS, vigilance sur les alertes du navigateur.
- Scan de ports : l’attaquant teste quels services sont ouverts pour trouver une faille. Contre-mesure : pare-feu, fermer les services inutiles.
- Attaque par force brute sur un service exposé : essais répétés de mots de passe. Contre-mesure : mots de passe forts, double authentification, limitation des tentatives.
- Déni de service (DoS) et déni de service distribué (DDoS) : saturer un service de requêtes pour le rendre indisponible. Contre-mesure : filtrage, services de protection.
- Compromission d’objets connectés : caméras, box, imprimantes avec des mots de passe par défaut.

Ces attaques touchent la confidentialité (écoute), l’intégrité (modification des échanges) et la disponibilité (saturation).

En résumé : écoute, interposition, faux Wi-Fi et saturation : le chiffrement et la bonne configuration réduisent la plupart de ces risques.

## Le Wi-Fi : comprendre et sécuriser
Le Wi-Fi diffuse vos données dans l’air. Sa sécurité dépend de la protection choisie.

Les normes de chiffrement :
- Réseau ouvert (sans mot de passe) : aucun chiffrement, tout peut être lu par d’autres.
- WEP : très ancien, cassé en quelques minutes. À ne jamais utiliser.
- WPA : ancien, remplacé.
- WPA2 : standard encore très répandu, solide avec un mot de passe long.
- WPA3 : le plus récent, plus résistant aux attaques par devinette du mot de passe. À préférer si votre matériel le permet.

Sécuriser le Wi-Fi de la maison :
1. Choisissez WPA2 ou WPA3, jamais WEP ni réseau ouvert.
2. Utilisez un mot de passe Wi-Fi long (une phrase de passe) et changez celui fourni par défaut s’il est facile à deviner.
3. Changez le mot de passe d’administration du routeur (pas celui du Wi-Fi) : par défaut il est souvent « admin ».
4. Mettez à jour le logiciel (firmware) du routeur.
5. Désactivez le WPS (connexion par bouton ou code PIN), qui est une faiblesse connue.
6. Désactivez l’administration à distance si vous n’en avez pas besoin.
7. Créez un réseau invité pour les visiteurs et les objets connectés, séparé de vos ordinateurs.
8. Vérifiez de temps en temps la liste des appareils connectés.

Masquer le nom du réseau (SSID) ne protège presque pas : ce n’est pas une vraie sécurité.

En résumé : WPA2 ou WPA3, mot de passe long, routeur à jour et réseau invité.

## Le pare-feu
Un pare-feu (firewall) est un filtre qui contrôle quel trafic est autorisé à entrer et à sortir d’un réseau ou d’un appareil, selon des règles.

On peut le comparer au gardien d’un immeuble : il vérifie qui entre, vers quelle porte (port), et peut refuser ceux qui ne sont pas attendus.

Les types principaux :
- Pare-feu personnel : intégré à votre système (Windows, macOS, Linux) et protège votre appareil.
- Pare-feu réseau : dans le routeur ou un équipement dédié, protège tout le réseau.
- Filtrage de paquets : décide d’après l’adresse, le port, le protocole.
- Pare-feu à états (stateful) : suit les connexions et n’accepte que les réponses à des demandes que vous avez faites.
- Pare-feu applicatif (WAF) : protège un site web contre des attaques visant l’application.

Les règles de base :
- Tout bloquer par défaut, autoriser seulement ce qui est nécessaire.
- Bloquer les connexions entrantes non sollicitées.
- Ne pas ouvrir de ports sur le routeur sans raison précise.

Limites : un pare-feu ne vous protège pas si vous téléchargez et lancez vous-même un malware, ni contre le phishing. Il fait partie de la défense en profondeur, il n’est pas suffisant seul.

Bonne pratique : gardez le pare-feu de votre système activé. Si une application vous demande de l’autoriser, demandez-vous si elle en a vraiment besoin.

En résumé : le pare-feu filtre le trafic selon des règles. Gardez-le activé et n’ouvrez que ce qui est nécessaire.

## Le chiffrement : HTTPS et certificats
Le chiffrement transforme des données lisibles en données illisibles sans la clé. Sur le web, c’est le rôle du protocole TLS, qui donne le « S » de HTTPS.

Ce que fait HTTPS :
- Confidentialité : personne sur le chemin ne peut lire les échanges.
- Intégrité : les données ne peuvent pas être modifiées sans que cela se voie.
- Authentification du serveur : un certificat prouve que vous parlez bien au site dont vous voyez l’adresse.

Le certificat : le site présente un certificat délivré par une autorité de certification reconnue par votre navigateur. Si le certificat est invalide, expiré ou ne correspond pas au site, le navigateur affiche un avertissement.

Ce qu’il ne faut pas croire :
- Le cadenas ne garantit pas que le site est honnête. Un site frauduleux peut aussi avoir un certificat valide pour son propre nom.
- Le chiffrement protège le trajet, pas ce que le site fait de vos données.

Bons réflexes :
1. Ne saisissez jamais de mot de passe ou de carte sur une page en HTTP seulement.
2. Si le navigateur affiche un avertissement de certificat, ne passez pas outre sans comprendre pourquoi, surtout sur un Wi-Fi public.
3. Vérifiez le nom de domaine, pas seulement le cadenas.
4. Mettez à jour votre navigateur.

Autres usages du chiffrement : messageries chiffrées de bout en bout, chiffrement du disque de votre ordinateur et de votre téléphone, sauvegardes chiffrées.

En résumé : HTTPS chiffre le trajet et prouve l’identité du serveur, mais n’indique pas que le site est de confiance.

## Le VPN
Un VPN (réseau privé virtuel) crée un tunnel chiffré entre votre appareil et un serveur VPN. Votre trafic traverse le réseau local dans ce tunnel, donc illisible pour les autres.

Ce qu’un VPN fait bien :
- Protège votre trafic sur un Wi-Fi public ou non fiable.
- Cache votre activité au gestionnaire du réseau local.
- Permet de se connecter à distance au réseau de l’entreprise ou de l’université.

Ce qu’un VPN ne fait pas :
- Il ne vous rend pas anonyme : le fournisseur du VPN voit votre trafic (s’il n’est pas lui-même chiffré).
- Il ne protège pas contre le phishing ni les malwares.
- Il ne rend pas un site non sécurisé plus sûr.
- Il n’empêche pas les sites de vous identifier si vous vous connectez à un compte.

Choisir un VPN :
- Méfiez-vous des VPN gratuits inconnus : s’ils ne vous font pas payer, ils vendent peut-être vos données.
- Préférez un fournisseur connu, transparent sur sa politique de conservation des journaux, et ayant fait l’objet d’audits indépendants.
- Utilisez celui de votre établissement ou de votre employeur quand il existe.

Un VPN est utile sur un réseau public, mais ce n’est pas un outil magique.

En résumé : un VPN chiffre le trajet entre votre appareil et le serveur VPN. Il aide sur les réseaux publics, sans remplacer les autres protections.

## Les réseaux publics
Un Wi-Fi de café, d’aéroport ou d’hôtel est partagé avec des inconnus et mal contrôlé.

Les risques :
- Faux Wi-Fi qui imite le vrai.
- Écoute du trafic si la connexion n’est pas chiffrée.
- Attaque de l’homme du milieu.
- Appareils voisins qui sondent les autres appareils du réseau.

Que faire :
1. Vérifiez le nom exact du réseau auprès du personnel.
2. Préférez la connexion de données mobiles pour les opérations sensibles (banque, paiement, comptes importants), ou utilisez un VPN de confiance.
3. Ne vous connectez qu’aux sites en HTTPS.
4. Désactivez le partage de fichiers et la connexion automatique aux réseaux ouverts.
5. Gardez votre système et votre navigateur à jour, et votre pare-feu activé.
6. Déconnectez-vous et oubliez le réseau après usage.
7. Ne saisissez pas de mot de passe si le navigateur affiche un avertissement.
8. Utilisez la double authentification : même si un mot de passe est intercepté, il ne suffit pas.

Un hotspot (partage de connexion) depuis votre téléphone, protégé par un mot de passe solide, est souvent plus sûr qu’un Wi-Fi public.

En résumé : sur un réseau public, vérifiez le nom, utilisez HTTPS et un VPN ou vos données mobiles pour le sensible.

## Les attaques par saturation (DoS et DDoS)
Une attaque par déni de service (DoS) vise à rendre un service indisponible en le submergeant de requêtes. Si les requêtes viennent de milliers d’appareils, on parle de DDoS (distribué).

On peut l’imaginer comme mille personnes qui bloquent l’entrée d’un magasin sans rien acheter : les vrais clients ne peuvent plus entrer.

D’où viennent les appareils attaquants : souvent d’un botnet, un réseau d’appareils infectés à l’insu de leurs propriétaires. En 2016, le botnet Mirai a infecté des centaines de milliers d’objets connectés (caméras, box) qui utilisaient des mots de passe par défaut. Il a servi à lancer des attaques massives, notamment contre un grand fournisseur de DNS (Dyn), ce qui a rendu inaccessibles plusieurs sites très connus pendant des heures.

Ce que cela nous apprend :
- Vos objets connectés peuvent être utilisés contre d’autres sans que vous le sachiez.
- Les mots de passe par défaut sont une faille majeure : changez-les.
- La disponibilité (le A de CIA) est une propriété de sécurité à part entière.

Défenses :
- Côté utilisateur : changer les mots de passe par défaut, mettre à jour, désactiver ce qui est inutile.
- Côté organisation : filtrage du trafic, services de protection anti-DDoS, capacité de réserve, plan de réaction.

En résumé : le DDoS sature un service avec des appareils piratés. Changer les mots de passe par défaut de vos objets connectés vous empêche d’en faire partie.

## Segmenter et surveiller
Dans une organisation, on ne met pas tous les appareils dans un seul réseau.

La segmentation consiste à diviser le réseau en zones séparées : par exemple un réseau pour les employés, un pour les invités, un pour les serveurs, un pour les objets connectés. Si un appareil est infecté, l’attaquant ne peut pas accéder directement à tout le reste.

À la maison, vous pouvez appliquer le même principe avec le réseau invité de votre box.

La surveillance :
- Journaux (logs) : trace des connexions et des actions, utile pour détecter et enquêter.
- IDS (système de détection d’intrusion) : repère un comportement suspect et alerte.
- IPS (système de prévention d’intrusion) : détecte et bloque automatiquement.
- Mises à jour et gestion des correctifs : ferment les failles connues.
- Contrôle des accès : seuls les appareils et personnes autorisés se connectent.
- Principe du moindre privilège appliqué au réseau : chaque appareil n’accède qu’à ce dont il a besoin.

Pourquoi c’est important : aucune protection n’est parfaite. La question n’est pas seulement « comment empêcher l’intrusion », mais aussi « comment la détecter vite et limiter ce qu’elle peut faire ». C’est la défense en profondeur.

En résumé : séparer les zones, surveiller, et donner à chaque appareil le minimum de droits.

## À retenir et auto-évaluation
Ce qu’il faut retenir :
- Un réseau comporte des adresses, un routeur, des ports et du DNS : chacun est un point à protéger.
- Les menaces : écoute, interposition, faux Wi-Fi, saturation.
- Wi-Fi : WPA2 ou WPA3, mot de passe long, routeur à jour, réseau invité.
- Le pare-feu filtre le trafic, HTTPS chiffre le trajet, le VPN protège sur les réseaux publics.
- Le cadenas HTTPS ne prouve pas l’honnêteté d’un site.
- Segmenter et surveiller limite les dégâts.

Testez-vous :

Q1. Pourquoi ne faut-il jamais utiliser le chiffrement WEP ?
Réponse : il est très ancien et peut être cassé en quelques minutes.

Q2. Qu’est-ce qu’un « evil twin » ?
Réponse : un faux point d’accès Wi-Fi qui porte le nom d’un réseau légitime pour attirer les victimes.

Q3. Que garantit le cadenas HTTPS, et que ne garantit-il pas ?
Réponse : il garantit que la connexion est chiffrée et que le serveur correspond au domaine. Il ne garantit pas que le site est honnête.

Q4. Citez deux limites d’un VPN.
Réponse : il ne rend pas anonyme (le fournisseur voit le trafic) et il ne protège pas contre le phishing ni les malwares.

Q5. Pourquoi est-il important de changer le mot de passe par défaut d’une caméra connectée ?
Réponse : sinon elle peut être piratée et intégrée à un botnet, comme avec Mirai en 2016.

Q6. À quoi sert un réseau invité sur votre box ?
Réponse : séparer les visiteurs et les objets connectés de vos ordinateurs, pour limiter les dégâts en cas de compromission.

Vous avez terminé le cours. Le dernier cours porte sur les mots de passe.',
5)
ON CONFLICT (id) DO UPDATE SET
    titre = EXCLUDED.titre,
    description = EXCLUDED.description,
    contenu = EXCLUDED.contenu,
    ordre = EXCLUDED.ordre;


INSERT INTO Courses (id, titre, description, contenu, ordre)
OVERRIDING SYSTEM VALUE VALUES (
6,
'Password Security',
'Créer, protéger et gérer ses mots de passe : longueur, gestionnaires, double authentification.',
'## Pourquoi les mots de passe comptent autant
Un mot de passe est la clé de votre vie numérique. Votre adresse e-mail est la plus importante : elle sert à réinitialiser presque tous vos autres comptes. Qui contrôle votre e-mail peut contrôler tout le reste.

Les mots de passe sont attaqués de nombreuses façons, et la plupart des attaques sont automatiques :
- Des programmes testent des millions de combinaisons par seconde.
- Des listes de mots de passe volés lors de fuites de données circulent et sont réutilisées.
- Le phishing et les logiciels espions les volent directement.

Les mots de passe les plus utilisés dans le monde chaque année sont toujours ridicules : 123456, password, azerty, qwerty, 111111. Ils sont cassés en une fraction de seconde.

Les problèmes les plus courants :
1. Mots de passe trop courts ou trop prévisibles.
2. Même mot de passe sur plusieurs sites : une fuite sur un petit site met en danger tous vos comptes.
3. Mots de passe partagés ou notés de façon visible.
4. Absence de double authentification.

Exemple : quelqu’un utilise le même mot de passe pour un forum de jeux et pour son e-mail. Le forum se fait pirater, l’attaquant essaie ce mot de passe sur l’e-mail et réussit. La personne n’a pourtant jamais été piégée directement.

En résumé : l’e-mail est la clé de tout, et un mot de passe faible ou réutilisé est la faille la plus facile à exploiter.

## Comment les attaquants obtiennent les mots de passe
Il existe plusieurs méthodes.

- Force brute : essayer toutes les combinaisons possibles. Très efficace contre les mots de passe courts.
- Attaque par dictionnaire : essayer des mots courants, des prénoms, des variations prévisibles (Mohamed2005, Azerty123!).
- Bourrage d’identifiants (credential stuffing) : prendre des paires e-mail/mot de passe volées sur un site et les tester automatiquement sur des centaines d’autres sites.
- Pulvérisation de mots de passe (password spraying) : essayer quelques mots de passe très courants sur un très grand nombre de comptes, pour éviter les blocages.
- Phishing : tromper la personne pour qu’elle donne elle-même son mot de passe.
- Keylogger : un logiciel espion enregistre ce que vous tapez.
- Fuite de données : un site est piraté et sa base d’utilisateurs est publiée.
- Observation directe : regarder par-dessus l’épaule, lire un mot de passe collé sur un écran.
- Devinette personnelle : utiliser vos informations publiques (date de naissance, prénom d’un proche).
- Réinitialisation : prendre le contrôle de l’e-mail ou du numéro de téléphone pour demander un nouveau mot de passe.

Ce que cela implique : un bon mot de passe ne protège pas contre tout. Il faut aussi la double authentification, un mot de passe unique par site, et de la vigilance face au phishing.

Pour savoir si une de vos adresses apparaît dans une fuite connue, vous pouvez utiliser le service Have I Been Pwned.

En résumé : force brute, dictionnaire, réutilisation, phishing et fuites sont les voies principales. Un mot de passe unique et long en bloque la plupart.

## La force d’un mot de passe : un peu de calcul
Pourquoi la longueur compte plus que la complexité ? Parce que chaque caractère supplémentaire multiplie le nombre de possibilités.

Nombre de combinaisons possibles :
- 8 lettres minuscules : 26 puissance 8, soit environ 200 milliards (2 x 10^11).
- 8 caractères mélangés (minuscules, majuscules, chiffres, symboles, environ 94 possibilités) : environ 6 millions de milliards (6 x 10^15).
- 12 caractères mélangés : environ 5 x 10^23, un nombre gigantesque.
- 4 mots choisis au hasard dans une liste de 7776 mots (méthode Diceware) : environ 3,7 millions de milliards (3,7 x 10^15).
- 5 mots au hasard : environ 2,8 x 10^19.

Vitesse d’un attaquant : contre certaines protections faibles, des ordinateurs puissants peuvent tester des milliards de combinaisons par seconde. Un mot de passe de 8 lettres minuscules peut donc être cassé en quelques secondes ou minutes, alors qu’un mot de passe de 12 caractères mélangés choisis au hasard demande un temps pratiquement impossible.

Attention : ces calculs valent pour des mots de passe vraiment aléatoires. Si vous choisissez « Mohamed2005! », l’attaquant ne teste pas toutes les combinaisons, il teste d’abord les prénoms, les années et les modèles habituels, et le trouve vite.

Conclusion : un mot de passe long et imprévisible vaut bien plus qu’un mot de passe court avec des symboles placés de façon prévisible.

En résumé : chaque caractère de plus multiplie le travail de l’attaquant. L’imprévisibilité et la longueur sont les clés.

## Ce qui fait un bon mot de passe
Voici les critères à retenir.

1. Longueur : au moins 12 caractères, idéalement 16 ou plus pour les comptes importants.
2. Imprévisibilité : pas de mot du dictionnaire seul, pas de prénom, pas de date, pas de suite (azerty, 123456, abcdef).
3. Unicité : un mot de passe différent par compte, surtout pour l’e-mail, la banque et les réseaux sociaux.
4. Aucun lien avec votre vie : nom, date de naissance, numéro, équipe de football, prénom d’un proche.
5. Pas de substitutions classiques : remplacer a par @ ou o par 0 ne trompe pas les programmes d’attaque, qui connaissent ces astuces.
6. Une base générée aléatoirement de préférence par un gestionnaire de mots de passe.

Ce qu’il faut éviter :
- Ajouter simplement un chiffre ou un point d’exclamation à la fin d’un mot (Motdepasse1!).
- Utiliser une même base avec une variation par site (Facebook2024, Instagram2024) : si un seul est connu, les autres sont faciles à deviner.
- Utiliser un mot de passe que vous avez déjà vu dans un exemple ou une liste.

Pour comparer deux mots de passe, posez-vous cette question : un programme qui connaîtrait mes habitudes pourrait-il le deviner ? Si oui, il est faible, même s’il paraît compliqué.

Test simple : si vous n’êtes pas capable de le retenir sans effort, c’est le rôle du gestionnaire de mots de passe de le retenir pour vous.

En résumé : long, imprévisible, unique et sans lien avec vous.

## Les phrases de passe
Une phrase de passe (passphrase) est une suite de plusieurs mots, longue et facile à retenir. Elle est excellente pour les mots de passe que vous devez mémoriser : le mot de passe maître de votre gestionnaire, le code de votre ordinateur, le mot de passe de votre e-mail.

Le principe :
1. Choisissez quatre à six mots au hasard, sans rapport entre eux (idéalement tirés au hasard avec des dés ou un générateur, pas inventés par vous).
2. Séparez-les par un symbole ou un chiffre, par exemple.
3. Évitez une phrase connue (une citation, un proverbe, des paroles de chanson).

Pourquoi ça marche : quatre mots tirés au hasard dans une grande liste donnent un nombre de combinaisons très élevé, et on les retient facilement en s’imaginant une image bizarre qui les relie.

Exemple de principe (ne l’utilisez pas tel quel) : une suite de quatre mots sans lien comme dans l’image d’une scène absurde.

Ce qui est faible : une phrase que vous avez composée avec vos goûts (« jaimelefootballetlebarcelone »), ou une citation célèbre. Les attaquants testent aussi des phrases connues.

Où utiliser quoi :
- Phrase de passe : pour ce que vous devez retenir (gestionnaire, appareil, e-mail).
- Mot de passe aléatoire de 16 caractères ou plus généré par le gestionnaire : pour tous les autres comptes.

En résumé : des mots tirés au hasard, assez nombreux, forment une phrase de passe solide et mémorisable.

## Comment un site doit stocker les mots de passe
Cette partie est utile pour comprendre ce qui se passe de l’autre côté, et pour votre projet CyberLab.

Un site sérieux ne stocke jamais votre mot de passe en clair. S’il était volé, tous les comptes seraient immédiatement compromis.

Il stocke un hachage (hash) : le résultat d’une fonction à sens unique. À partir du mot de passe, on calcule une empreinte, mais il est quasiment impossible de retrouver le mot de passe à partir de l’empreinte.

À la connexion, le site calcule l’empreinte du mot de passe saisi et la compare à celle stockée. Il n’a jamais besoin de connaître le mot de passe.

Deux protections supplémentaires :
- Le sel (salt) : une valeur aléatoire propre à chaque utilisateur, ajoutée avant le calcul. Deux utilisateurs ayant le même mot de passe auront des empreintes différentes, ce qui empêche les attaques par tables précalculées (rainbow tables).
- Un algorithme lent et conçu pour les mots de passe : bcrypt, scrypt, Argon2. Il est volontairement lent à calculer, ce qui rend les essais massifs très coûteux. Les fonctions rapides comme MD5 ou SHA-1 seules sont à éviter pour cet usage.

Cas réel : en 2012, le réseau professionnel LinkedIn a subi une fuite, et des millions de mots de passe protégés par une fonction rapide et sans sel ont été publiés ; une grande partie a été retrouvée rapidement.

Dans CyberLab, les mots de passe sont hachés avec BCrypt, qui inclut déjà un sel. Ils ne sont jamais enregistrés en clair, et les mots de passe testés dans la simulation ne sont jamais enregistrés du tout.

En résumé : un site sérieux stocke un hachage salé avec un algorithme lent comme BCrypt, jamais le mot de passe lui-même.

## Les gestionnaires de mots de passe
Il est impossible de retenir des dizaines de mots de passe longs et uniques. La solution est un gestionnaire de mots de passe : un coffre chiffré qui les génère, les stocke et les remplit à votre place.

Comment cela fonctionne :
- Vous retenez un seul mot de passe maître, très solide (une phrase de passe).
- Le gestionnaire crée un mot de passe aléatoire et unique pour chaque site.
- Il remplit automatiquement les champs, uniquement sur le bon site, ce qui aide à repérer un faux site de phishing.

Avantages :
- Un mot de passe unique et fort partout, sans effort.
- Protection contre la réutilisation.
- Alerte en cas de mot de passe faible, répété ou apparu dans une fuite (selon les outils).

Exemples de gestionnaires reconnus : Bitwarden, KeePassXC (gratuit et local), 1Password. Les navigateurs et les systèmes en intègrent aussi, plus ou moins complets.

Risques et précautions :
- Le mot de passe maître doit être très solide et ne servir qu’à cela.
- Activez la double authentification sur le gestionnaire lui-même.
- Sauvegardez le moyen de récupération (clé de secours) dans un endroit sûr.
- Ne stockez pas vos mots de passe dans un fichier texte, un tableau ou des notes sans protection.

Certains craignent de « tout mettre au même endroit ». Le risque réel d’un gestionnaire bien configuré est bien plus faible que celui de réutiliser le même mot de passe partout.

En résumé : un gestionnaire retient pour vous des mots de passe longs et uniques ; protégez-le avec une phrase de passe solide et la double authentification.

## La double authentification et les passkeys
La double authentification (2FA, ou authentification multifacteur) ajoute une deuxième preuve : en plus de ce que vous savez (le mot de passe), il faut ce que vous possédez ou ce que vous êtes.

Les méthodes, de la plus faible à la plus solide :
1. Code par SMS : mieux que rien, mais vulnérable au détournement de numéro (SIM swap) et à l’interception. À éviter si une meilleure option existe.
2. Code d’une application d’authentification (TOTP) : un code à six chiffres qui change toutes les 30 secondes. Bien plus sûr que le SMS.
3. Notification d’approbation dans une application : pratique, mais attention à ne pas approuver par réflexe une demande que vous n’avez pas faite (fatigue des notifications).
4. Clé de sécurité physique (FIDO2, par exemple une clé USB) : la meilleure protection contre le phishing, car elle ne fonctionne qu’avec le vrai site.
5. Passkeys : un système moderne qui remplace le mot de passe par une clé cryptographique liée à votre appareil et déverrouillée par votre empreinte ou votre code. Il résiste au phishing et supprime le besoin de retenir un mot de passe pour les sites qui le proposent.

Où l’activer en priorité :
- Votre e-mail principal.
- Votre banque et vos services de paiement.
- Vos réseaux sociaux.
- Votre gestionnaire de mots de passe.
- Vos comptes de travail ou d’études.

Précaution : conservez les codes de secours fournis lors de l’activation, au cas où vous perdriez votre téléphone. Ne donnez jamais un code de double authentification à quelqu’un, même si la personne affirme être du support : c’est une attaque classique.

En résumé : la double authentification bloque la plupart des prises de contrôle ; préférez une application ou une clé de sécurité au SMS.

## En cas de fuite ou de compromission
Si un de vos comptes a été piraté, ou si vous apprenez qu’il apparaît dans une fuite, agissez dans cet ordre.

1. Changez immédiatement le mot de passe du compte concerné, depuis un appareil sain.
2. Changez aussi les mots de passe de tous les comptes où vous utilisiez le même (ou un très semblable), en commençant par l’e-mail et la banque.
3. Activez la double authentification partout où c’est possible.
4. Déconnectez les autres sessions : la plupart des services proposent « se déconnecter de tous les appareils ».
5. Vérifiez l’activité récente : connexions inconnues, messages envoyés, achats, changements de paramètres, règles de transfert dans l’e-mail.
6. Vérifiez les moyens de récupération : adresse e-mail et numéro de téléphone de secours, qui peuvent avoir été modifiés par l’attaquant.
7. Prévenez vos contacts si le compte a pu envoyer des messages à votre place.
8. Surveillez votre banque si des informations de paiement étaient concernées, et faites opposition si nécessaire.
9. Lancez une analyse antivirus : si un logiciel espion a volé le mot de passe, en changer ne suffit pas.
10. Utilisez un gestionnaire pour que cela ne se reproduise pas.

Ne vous culpabilisez pas : les fuites arrivent même aux sites les mieux protégés. Ce qui compte, c’est que la fuite d’un site ne mette pas en danger vos autres comptes. Si chaque mot de passe est unique, l’impact reste limité à un seul compte.

En résumé : changer, étendre, activer la double authentification, déconnecter les sessions, vérifier l’activité.

## Les idées reçues sur les mots de passe
Certaines habitudes très répandues sont en réalité inutiles ou nuisibles.

Mythe 1 : « Il faut changer ses mots de passe tous les 3 mois. »
En réalité : les recommandations actuelles (par exemple celles du NIST) déconseillent le changement périodique obligatoire. Les gens choisissent alors des variations prévisibles (Motdepasse1, Motdepasse2). Il vaut mieux changer un mot de passe quand il y a un soupçon de compromission.

Mythe 2 : « Un mot de passe avec des symboles est automatiquement fort. »
En réalité : P@ssw0rd! est une des premières variantes testées. La longueur et l’imprévisibilité comptent plus.

Mythe 3 : « Je n’ai rien d’intéressant, personne ne me piratera. »
En réalité : beaucoup d’attaques sont automatiques. Votre compte a de la valeur : pour usurper votre identité, envoyer du spam, arnaquer vos contacts.

Mythe 4 : « Les questions de sécurité (nom de jeune fille de la mère, ville de naissance) protègent mon compte. »
En réalité : les réponses sont souvent publiques ou devinables. Si possible, utilisez de fausses réponses aléatoires stockées dans le gestionnaire.

Mythe 5 : « Mon navigateur retient mon mot de passe, donc c’est dangereux. »
En réalité : un gestionnaire intégré, protégé par le verrouillage de votre session, est bien meilleur que de réutiliser le même mot de passe. L’idéal reste un gestionnaire dédié avec double authentification.

Mythe 6 : « Je peux partager mon mot de passe avec un ami de confiance. »
En réalité : vous perdez le contrôle (il peut le noter, le réutiliser, se faire pirater lui-même). Utilisez les fonctions de partage ou de délégation des services quand elles existent.

En résumé : longueur et unicité valent mieux que symboles et changements fréquents.

## Mise en pratique et auto-évaluation
Exercice 1 : classez ces mots de passe du plus faible au plus fort, en expliquant.
a) azerty123
b) Mohamed2005!
c) Tr0ub4dor&3
d) un mot de passe aléatoire de 16 caractères généré par un gestionnaire
e) quatre mots au hasard séparés par des tirets

Réponse : a) très faible, courant et prévisible. b) faible, prénom + année + symbole attendu. c) moyen, court et suit des motifs classiques. e) fort, long et mémorisable si les mots sont vraiment aléatoires. d) très fort, mais à confier à un gestionnaire.

Exercice 2 : votre e-mail utilise le même mot de passe que le site d’un club sportif qui vient d’annoncer une fuite de données. Que faites-vous, dans l’ordre ?
Réponse : changer tout de suite le mot de passe de l’e-mail (puis celui du club), activer la double authentification sur l’e-mail, vérifier l’activité récente, changer les autres comptes qui utilisent le même mot de passe.

Q1. Pourquoi la longueur est-elle plus importante que les symboles ?
Réponse : chaque caractère supplémentaire multiplie le nombre de combinaisons, alors que des symboles placés de façon prévisible ne compliquent presque pas la tâche.

Q2. Qu’est-ce que le credential stuffing ?
Réponse : tester automatiquement sur de nombreux sites des identifiants volés ailleurs, en comptant sur la réutilisation des mots de passe.

Q3. À quoi sert le sel dans le hachage d’un mot de passe ?
Réponse : à produire une empreinte différente pour deux mots de passe identiques, et à empêcher les attaques par tables précalculées.

Q4. Quel est le principal inconvénient de la double authentification par SMS ?
Réponse : elle est vulnérable au détournement de numéro et à l’interception. Une application ou une clé de sécurité est plus sûre.

Q5. Pourquoi les recommandations actuelles déconseillent-elles de changer son mot de passe tous les trois mois ?
Réponse : les gens choisissent alors des variations prévisibles. Il vaut mieux un mot de passe fort et unique, changé en cas de compromission.

Q6. Quelle est la meilleure protection contre le phishing parmi les méthodes de double authentification ?
Réponse : la clé de sécurité physique (FIDO2) ou les passkeys, qui ne fonctionnent qu’avec le vrai site.

Vous avez terminé tous les cours. Testez vos habitudes avec la simulation Password Strength Checker, en utilisant uniquement un mot de passe de test, jamais un vrai.',
6)
ON CONFLICT (id) DO UPDATE SET
    titre = EXCLUDED.titre,
    description = EXCLUDED.description,
    contenu = EXCLUDED.contenu,
    ordre = EXCLUDED.ordre;

-- Remet le compteur automatique de id après le plus grand id (sans effet si la colonne n'est pas automatique).
SELECT setval(pg_get_serial_sequence('courses', 'id'), (SELECT MAX(id) FROM courses));
