package org.plugins;

import oop.if2210_tb2_sc4.*;
import oop.if2210_tb2_sc4.Deck;
import oop.if2210_tb2_sc4.card.*;
import oop.if2210_tb2_sc4.save_load.SaveLoad;
import oop.if2210_tb2_sc4.save_load.SaveLoadAnnotation;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

@SaveLoadAnnotation(type = "XML")
public class SaveLoadXML implements SaveLoad {
    private Element gameState = null;
    private Element player1 = null;
    private Element player2 = null;
    private String folderName = "";
    private Document newDocs = null;

    public SaveLoadXML(){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            this.newDocs = builder.newDocument();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    public SaveLoadXML(String folderName) {
        try {
            // Create a DocumentBuilderFactory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            // Create a DocumentBuilder
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Parse the XML file and get the document
            URL url = SaveLoad.class.getResource("");
            assert url != null;
            String cwd = url.getPath().startsWith("/") ? url.getPath().substring(1) : url.getPath();
            Document document = builder.parse(new File(cwd + folderName + "/state.xml"));

            // Normalize the XML structure
            document.getDocumentElement().normalize();

            // Create new document to write
            this.newDocs = builder.newDocument();

            // Get the root element
            Element root = document.getDocumentElement();
            System.out.println("Root Element: " + root.getNodeName());

            // Parse player1
            NodeList player1List = document.getElementsByTagName("player1");
            if (player1List.getLength() > 0) {
                Node player1Node = player1List.item(0);
                if (player1Node.getNodeType() == Node.ELEMENT_NODE) {
                    this.player1 = (Element) player1Node;
                }
            }

            // Parse player2
            NodeList player2List = document.getElementsByTagName("player2");
            if (player2List.getLength() > 0) {
                Node player2Node = player2List.item(0);
                if (player2Node.getNodeType() == Node.ELEMENT_NODE) {
                    this.player2 = (Element) player2Node;
                }
            }

            // Parse gameState
            NodeList gameStateList = document.getElementsByTagName("gameState");
            if (gameStateList.getLength() > 0) {
                Node gameStateNode = gameStateList.item(0);
                if (gameStateNode.getNodeType() == Node.ELEMENT_NODE) {
                    this.gameState = (Element) gameStateNode;
                }
            }

            this.folderName = folderName;

            GameData.initCards();
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                this.newDocs = builder.newDocument();
                this.folderName = folderName;

                URL url = SaveLoad.class.getResource("");
                assert url != null;
                String cwd = url.getPath().startsWith("/") ? url.getPath().substring(1) : url.getPath();
                handleNewFile(Paths.get(cwd + folderName + "/state.xml"));
            } catch (Exception e1) {
                System.out.println("Error inside: " + e1.getMessage());
            }
        }
    }

    public void loadGameState() {
        if (this.gameState != null) {
            parseGameState(this.gameState);
        }
    }

    public void loadPlayer(int var1) {
        if (var1 == 1 && this.player1 != null) {
            parsePlayer(this.player1, 1);
            return;
        } else if (var1 == 2 && this.player2 != null) {
            parsePlayer(this.player2, 2);
            return;
        }
        System.out.println("Failed to load player " + var1);
    }

    private static void parsePlayer(@NotNull Element playerElement, int no_player) {
        System.out.println();
        System.out.println("Parsing player");
        System.out.println();

        Player player = new Player();
        Deck deck = new Deck();
        Ladang ladang = new Ladang();

        // Parse gulden and deckCount
        int Gulden = Integer.parseInt(playerElement.getElementsByTagName("gulden").item(0).getTextContent());
        System.out.println("Gulden: " + Gulden);
        player.setJumlahGulden(Gulden);
        int deckCount = Integer.parseInt(playerElement.getElementsByTagName("deckCount").item(0).getTextContent());
        System.out.println("Deck count: " + deckCount);
        deck.setCardsInDeckCount(deckCount);

        // Parse hand
        NodeList handList = playerElement.getElementsByTagName("hand");
        if (handList.getLength() > 0) {
            Element handElement = (Element) handList.item(0);

            int handCount = Integer.parseInt(handElement.getAttribute("count"));
            System.out.println("Hand count: " + handCount);
            deck.setCardsInHandCount(handCount);

            NodeList cardList = handElement.getElementsByTagName("card");
            for (int i = 0; i < cardList.getLength(); i++) {
                Node cardNode = cardList.item(i);
                if (cardNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element cardElement = (Element) cardNode;

                    String lokasi = cardElement.getElementsByTagName("lokasi").item(0).getTextContent();
                    System.out.println("Card lokasi: " + lokasi);

                    String nama = cardElement.getElementsByTagName("name").item(0).getTextContent();
                    System.out.println("Card name: " + nama);

                    deck.setActiveCard(lokasi, GameData.createCard(nama));
                }
            }
        }
        player.setDeck(deck);

        // Parse ladang
        NodeList ladangList = playerElement.getElementsByTagName("ladang");
        if (ladangList.getLength() > 0) {
            Element ladangElement = (Element) ladangList.item(0);

            int ladangCount = Integer.parseInt(ladangElement.getAttribute("count"));
            System.out.println("Ladang count: " + ladangCount);

            NodeList itemList = ladangElement.getElementsByTagName("item");
            for (int i = 0; i < ladangCount; i++) {
                Node itemNode = itemList.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element itemElement = (Element) itemNode;

                    String name = itemElement.getElementsByTagName("name").item(0).getTextContent();
                    System.out.println("Item name: " + name);

                    String lokasi = itemElement.getElementsByTagName("lokasi").item(0).getTextContent();
                    System.out.println("Item lokasi: " + lokasi);

                    int ageOrWeight = Integer.parseInt(itemElement.getElementsByTagName("ageOrWeight").item(0).getTextContent());
                    System.out.println("Item ageOrWeight: " + ageOrWeight);

                    FarmResourceCard card = (FarmResourceCard) GameData.createCard(name);
                    assert card != null : "Card not found";

                    // Set age or weight
                    if (card instanceof PlantCard) {
                        ((PlantCard) card).setAge(ageOrWeight);
                    } else if (card instanceof AnimalCard) {
                        ((AnimalCard) card).setWeight(ageOrWeight);
                    }

                    // parse Effect
                    NodeList effectList = itemElement.getElementsByTagName("effect");
                    System.out.println("Effects count: " + effectList.getLength());
                    for (int j = 0; j < effectList.getLength(); j++) {
                        String effect = effectList.item(j).getTextContent();
                        System.out.println("Effect: " + effect);

                        if (Objects.equals(effect, "ACCELERATE")){
                            card.addEffect(EffectType.ACCELERATE);
                        } else if (Objects.equals(effect, "DELAY")){
                            card.addEffect(EffectType.DELAY);
                        }  else if (Objects.equals(effect, "INSTANT_HARVEST")){
                            card.addEffect(EffectType.INSTANT_HARVEST);
                        } else if (Objects.equals(effect, "PROTECT")){
                            card.addEffect(EffectType.PROTECT);
                        } else if (Objects.equals(effect, "TRAP")){
                            card.addEffect(EffectType.TRAP);
                        }
                    }

                    ladang.setCard(lokasi, card);
                }
            }
        }

        player.setLadang(ladang);
        GameState.getInstance().setPlayer(no_player, player);
    }

    private static void parseGameState(@NotNull Element gameStateElement) {
        System.out.println();
        System.out.println("Parsing game state");
        System.out.println();
        Shop shop = new Shop();

        // Parse items
        NodeList itemsList = gameStateElement.getElementsByTagName("items");
        if (itemsList.getLength() > 0) {
            Element itemsElement = (Element) itemsList.item(0);
            System.out.println("Items count: " + itemsElement.getAttribute("count"));
            NodeList itemList = itemsElement.getElementsByTagName("item");
            for (int i = 0; i < itemList.getLength(); i++) {
                Node itemNode = itemList.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element itemElement = (Element) itemNode;

                    int count = Integer.parseInt(itemElement.getElementsByTagName("count").item(0).getTextContent());
                    System.out.println("Item count: " + count);
                    String name = itemElement.getElementsByTagName("name").item(0).getTextContent();
                    System.out.println("Item name: " + name);

                    shop.addCard((ProductCard) GameData.getCard(name), count);
                }
            }
        }
        GameState.getInstance().setShop(shop);

        // Parse currentPlayer
        int currentPlayer = Integer.parseInt(gameStateElement.getElementsByTagName("currentPlayer").item(0).getTextContent());
        System.out.println("Current player: " + currentPlayer);
        GameState.getInstance().setCurrentPlayer(currentPlayer);
    }

    @Override
    public void save(String folderName) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            newDocs = builder.newDocument();
            /* Create root element */
            Element root = newDocs.createElement("game");
            newDocs.appendChild(root);

            /* Game State Element */
            Element gameState = newDocs.createElement("gameState");
            root.appendChild(gameState);

            // Append Current Player
            Element currentPlayer = newDocs.createElement("currentPlayer");
            currentPlayer.appendChild(newDocs.createTextNode(String.valueOf(GameState.getInstance().getCurrentPlayer())));
            gameState.appendChild(currentPlayer);

            // Append Items
            Element items = newDocs.createElement("items");
            items.setAttribute("count", String.valueOf(GameState.getInstance().getCountItems()));
            gameState.appendChild(items);

            for (Map.Entry<ProductCard, Integer> i : GameState.getInstance().getShopItems().entrySet()) {
                if (i.getValue().equals(0)) continue;

                ProductCard card = i.getKey();
                Element item = newDocs.createElement("item");

                Element count = newDocs.createElement("count");
                count.appendChild(newDocs.createTextNode(String.valueOf(i.getValue())));
                item.appendChild(count);

                Element name = newDocs.createElement("name");
                name.appendChild(newDocs.createTextNode(card.getName()));
                item.appendChild(name);

                items.appendChild(item);
            }

            /* Player1 Element */
            Element player1 = newDocs.createElement("player1");
            root.appendChild(player1);

            Player p1 = GameState.getInstance().getPlayer(1);

            // Append Gulden
            Element gulden1 = newDocs.createElement("gulden");
            gulden1.appendChild(newDocs.createTextNode(String.valueOf(p1.getJumlahGulden())));
            player1.appendChild(gulden1);

            // Append Deck Count
            Element deckCount1 = newDocs.createElement("deckCount");
            deckCount1.appendChild(newDocs.createTextNode(String.valueOf(p1.getDeck().getCardsInDeckCount())));
            player1.appendChild(deckCount1);

            // Append Hand
            Element hand1 = newDocs.createElement("hand");
            player1.appendChild(hand1);
            Deck deck1 = p1.getDeck();
            setHand(player1, hand1, deck1);

            // Append Ladang
            Element ladangElement1 = newDocs.createElement("ladang");
            ladangElement1.setAttribute("count", String.valueOf(p1.getLadang().getCardinLadangCount()));
            player1.appendChild(ladangElement1);
            setItemLadang(ladangElement1, p1.getLadang().getAllCardwithLocationinLadang());

            /* Player2 Element */
            Element player2 = newDocs.createElement("player2");
            root.appendChild(player2);

            Player p2 = GameState.getInstance().getPlayer(2);

            // Append Gulden
            Element gulden2 = newDocs.createElement("gulden");
            gulden2.appendChild(newDocs.createTextNode(String.valueOf(p2.getJumlahGulden())));
            player2.appendChild(gulden2);

            // Append Deck Count
            Element deckCount2 = newDocs.createElement("deckCount");
            deckCount2.appendChild(newDocs.createTextNode(String.valueOf(p2.getDeck().getCardsInDeckCount())));
            player2.appendChild(deckCount2);

            // Append Hand
            Element hand2 = newDocs.createElement("hand");
            player2.appendChild(hand2);
            Deck deck2 = p2.getDeck();
            setHand(player2, hand2, deck2);

            // Append Ladang
            Element ladangElement2 = newDocs.createElement("ladang");
            ladangElement2.setAttribute("count", String.valueOf(p2.getLadang().getCardinLadangCount()));
            player2.appendChild(ladangElement2);
            setItemLadang(ladangElement2, p2.getLadang().getAllCardwithLocationinLadang());

            // Write to XML File
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("indent", "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(newDocs);

            // Specify the file path
            URL url = SaveLoad.class.getResource("");
            assert url != null;
            String cwd = url.getPath().startsWith("/") ? url.getPath().substring(1) : url.getPath();
            System.out.println(cwd + folderName);
            StreamResult filePath = new StreamResult(handleNewFile(Paths.get(cwd + folderName + "/state.xml")));
            transformer.transform(source, filePath);

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void setHand(Element player1, Element hand, Deck deck) {
        hand.setAttribute("count", String.valueOf(deck.getActiveCardinHandCount()));
        for (int i=0; i < deck.getActiveCardinHandCount(); i++) {
            Element cardElement = newDocs.createElement("card");
            cardElement
                    .appendChild(newDocs.createElement("lokasi"))
                    .appendChild(newDocs.createTextNode( (char) ('A' + i) + "01" ));
            cardElement
                    .appendChild(newDocs.createElement("name"))
                    .appendChild(newDocs.createTextNode(deck.getActiveCards()[i].getName()));
            hand.appendChild(cardElement);
        }
        player1.appendChild(hand);
    }

    private void setItemLadang(Element ladangElement, Map<String, FarmResourceCard> cardInLadang) {
        for(Map.Entry<String, FarmResourceCard> entry : cardInLadang.entrySet()) {
            FarmResourceCard card = entry.getValue();
            Element item = newDocs.createElement("item");
            item
                    .appendChild(newDocs.createElement("name"))
                    .appendChild(newDocs.createTextNode(card.getName()));
            item
                    .appendChild(newDocs.createElement("lokasi"))
                    .appendChild(newDocs.createTextNode(entry.getKey()));

            int ageOrWeight = 0;
            if (card instanceof PlantCard) {
                ageOrWeight = ((PlantCard) card).getAge();
            } else if (card instanceof AnimalCard) {
                ageOrWeight = ((AnimalCard) card).getWeight();
            }
            item
                    .appendChild(newDocs.createElement("ageOrWeight"))
                    .appendChild(newDocs.createTextNode(String.valueOf(ageOrWeight)));

            Element effects = newDocs.createElement("effects");
            effects.setAttribute("count", String.valueOf(card.getEffect().size()));
            for (EffectType effect : card.getEffect()) {
                effects
                        .appendChild(newDocs.createElement("effect"))
                        .appendChild(newDocs.createTextNode(effect.toString()));
            }
            item.appendChild(effects);

            ladangElement.appendChild(item);
        }
    }

    private File handleNewFile(Path path){
        File file = null;
        try {
            // if folder does not exist, create new file
            Files.createDirectories(path.getParent());

            file = new File(path.toString());
            if (!file.exists()) {
                // if file does not exist, create new file
                if (file.createNewFile()) {
                    System.out.println("File created: " + file.getName());
                } else {
                    System.out.println("File creating failed.");
                }
            } else {
                System.out.println("File " + file.getName() + " already exists.");
                System.out.println("Save will overwrite the file.");
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            System.out.println();
        }
        return file;
    }
}

