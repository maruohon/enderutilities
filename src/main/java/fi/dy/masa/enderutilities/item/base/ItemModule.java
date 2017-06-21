package fi.dy.masa.enderutilities.item.base;


public abstract class ItemModule extends ItemEnderUtilities implements IModule
{
    public ItemModule(String name)
    {
        super(name);
    }

    public enum ModuleType
    {
        // The name is used for storing the selected module in modular items
        TYPE_ENDERCORE              (0, "endercore"),
        TYPE_ENDERCAPACITOR         (1, "endercapacitor"),
        TYPE_LINKCRYSTAL            (2, "linkcrystal"),
        TYPE_MOBPERSISTENCE         (3, "mobpersistence"),
        TYPE_MEMORY_CARD_MISC       (4, "memorycard_misc"),
        TYPE_MEMORY_CARD_ITEMS      (5, "memorycard_items"),
        CREATIVE_BREAKING           (6, "creative_breaking"),
        TYPE_ANY                    (-10, "any"),
        TYPE_INVALID                (-1, "invalid");

        private final int index;
        private final String name;

        ModuleType(int index, String name)
        {
            this.index = index;
            this.name = name;
        }

        public int getIndex()
        {
            return this.index;
        }

        public String getName()
        {
            return this.name;
        }

        public boolean equals(ModuleType val)
        {
            return val.getIndex() == this.index;
        }
    }
}
