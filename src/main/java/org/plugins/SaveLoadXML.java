package org.plugins;

import oop.if2210_tb2_sc4.*;
import oop.if2210_tb2_sc4.Deck;
import oop.if2210_tb2_sc4.card.*;
import oop.if2210_tb2_sc4.save_load.Load;
import oop.if2210_tb2_sc4.save_load.Save;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Objects;


public class SaveLoadXML implements Save, Load {
    private Element gameState = null;
    private Element player1 = null;
    private Element player2 = null;
    private String folderName = "";

    public SaveLoadXML(String folderName) {
        try {
            // Create a DocumentBuilderFactory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            // Create a DocumentBuilder
            DocumentBuilder builder = factory.newDocumentBuilder();

            // Parse the XML file and get the document
            Document document = builder.parse("src/main/java/org/plugins/" + folderName + "/state.xml");

            // Normalize the XML structure
            document.getDocumentElement().normalize();

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

    public void save() {
        // TODO: IMPLEMENT SAVE TO XML
    }
}

