package fi.dy.masa.enderutilities.item.base;




public abstract class ItemModule extends ItemEnderUtilities implements IModule
{
    public ItemModule()
    {
        super();
    }

    public enum ModuleType
    {
        // The name is used for storing the selected module in modular items
        TYPE_ENDERCORE_ACTIVE       (0, "endercore"),
        TYPE_ENDERCORE_INACTIVE     (1, "endercore_inactive"),
        TYPE_ENDERCAPACITOR         (2, "endercapacitor"),
        TYPE_LINKCRYSTAL            (3, "linkcrystal"),
        TYPE_MOBPERSISTENCE         (4, "mobpersistence"),
        TYPE_MEMORY_CARD            (5, "memorycard"),
        TYPE_ANY                    (-1, "any"),
        TYPE_INVALID                (-10, "invalid");

        private final int index;
        private final String name;

        ModuleType(int index, String name)
        {
            this.index = index;
            this.name = name;
        }

        public int getOrdinal()
        {
            return this.index;
        }

        public String getName()
        {
            return this.name;
        }

        public boolean equals(ModuleType val)
        {
            return val.getOrdinal() == this.index;
        }
    }
}
