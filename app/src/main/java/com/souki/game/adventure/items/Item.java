package com.souki.game.adventure.items;

import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Created by gwalarn on 27/11/16.
 */

public class Item {

    public final static String HELP_PAGE_ITEM = "Scroll";

    public enum ItemTypeID {
        Banana,
        Barrel,
        Book,
        BookDark,
        Boots,
        ChessClosed,
        ChessOpen,
        Compass,
        CrystalAnimBlue,
        CrystalAnimGreen,
        CrystalAnimGrey,
        CrystalAnimPink,
        CrystalAnimYellow,
        CrystalsBlue,
        CrystalsGreen,
        CrystalsPurple,
        Dagger,
        DaggerBroken,
        FoodAle,
        FoodBiscuit,
        FoodBread,
        FoodBread2,
        FoodShank,
        GameCards,
        Gear,
        Gloves,
        GlyphGreen,
        GlyphRed,
        GlyphYellow,
        Guitar,
        Hammer,
        HammerSmall,
        Jewels,
        JewelsBlue,
        JewelsRed,
        KeyCrystal,
        KeyDark,
        KeyIron,
        KeyMoon,
        KeyRed,
        KeyRoyal,
        Lantern,
        Lantern2,
        Letter,
        Mirror,
        NecklaceJewelBlue,
        NecklaceJewelGreen,
        NecklaceJewelOrange,
        NecklaceJewelRed,
        Pick,
        PotionBlueSmall,
        PotionGreenSmall,
        PotionRedSmall,
        PotionRounededBrown,
        PotionSilver,
        PotionTealBig,
        PotionVioletLarge,
        PotionYellowLarge,
        PotionYellowSmall,
        RingGold,
        RobeBlue,
        Scroll01,
        Scroll02,
        Scroll03,
        Scroll04,
        Scroll05,
        Scroll06,
        Scroll07,
        Scroll08,
        Scroll09,
        Scroll10,
        Scroll11,
        Scroll12,
        Scroll13,
        Scroll14,
        Stone1,
        Stone2,
        Stone3,
        Stone4,
        Stone5,
        Stone6,
        Stone7,
        Stone8,
        Stone9,
        Stone10,
        Stone11,
        Stone12,
        Stone13,
        Stone14,
        Stone15,
        Stone16,
        Shovel,
        Shovel2,
        TeddyBear,
        Torch,
        TorchFire,
        VialBlue,
        VialGreen,
        VialRed
    }

    private ItemTypeID itemTypeID;
    private String itemShortDescription;
    private TextureRegion mTextureRegion;


    public Item(TextureRegion textureRegion, ItemTypeID itemTypeID) {
        mTextureRegion = textureRegion;

        this.itemTypeID = itemTypeID;

    }

    public Item() {
        super();
    }

    public Item(ItemTypeID inventoryItemType) {
        super();
        this.itemTypeID = inventoryItemType;
    }

    public Item(Item inventoryItem) {
        super();
        this.itemTypeID = inventoryItem.getItemTypeID();
        this.itemShortDescription = inventoryItem.getItemShortDescription();

    }

    public ItemTypeID getItemTypeID() {
        return itemTypeID;
    }

    public void setItemTypeID(ItemTypeID itemTypeID) {
        this.itemTypeID = itemTypeID;
    }


    public String getItemShortDescription() {
        return itemShortDescription;
    }

    public void setItemShortDescription(String itemShortDescription) {
        this.itemShortDescription = itemShortDescription;
    }


    public boolean isSameItemType(Item candidateInventoryItem) {
        return itemTypeID == candidateInventoryItem.getItemTypeID();
    }

    public TextureRegion getTextureRegion() {
        return mTextureRegion;
    }

    public void setTextureRegion(TextureRegion textureRegion) {
        mTextureRegion = textureRegion;
    }

}

