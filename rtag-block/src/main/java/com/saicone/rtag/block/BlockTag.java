package com.saicone.rtag.block;

import com.saicone.rtag.tag.TagCompound;
import com.saicone.rtag.util.EasyLookup;
import com.saicone.rtag.util.ServerInstance;

import java.lang.invoke.MethodHandle;

public class BlockTag {

    private static final MethodHandle save;
    private static final MethodHandle load;

    static {
        MethodHandle m1 = null, m2 = null;
        try {
            // Old method names
            String save = "b", load = "a";
            // New method names
            if (ServerInstance.verNumber >= 18) {
                save = "m";
            } else if (ServerInstance.verNumber >= 9) {
                save = "save";
                if (ServerInstance.verNumber >= 12) {
                    load = "load";
                }
            }

            if (ServerInstance.verNumber >= 18) {
                m1 = EasyLookup.method("TileEntity", save, "NBTTagCompound");
            } else {
                // Unreflect reason:
                // (1.8) void method
                // Other versions return NBTTagCompound
                m1 = EasyLookup.unreflectMethod("TileEntity", save, "NBTTagCompound");
            }
            if (ServerInstance.verNumber == 16) {
                m2 = EasyLookup.method("TileEntity", load, void.class, "IBlockData", "NBTTagCompound");
            } else {
                m2 = EasyLookup.method("TileEntity", load, void.class, "NBTTagCompound");
            }
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
        }
        save = m1; load = m2;
    }

    public static Object saveTag(Object tile) throws Throwable {
        if (ServerInstance.verNumber >= 18) {
            return save.invoke(tile);
        } else if (ServerInstance.verNumber >= 9) {
            return save.invoke(tile, TagCompound.newTag());
        } else {
            Object tag = TagCompound.newTag();
            save.invoke(tile, tag);
            return tag;
        }
    }

    public static void loadTag(Object tile, Object tag) throws Throwable {
        if (ServerInstance.verNumber == 16) {
            load.invoke(tile, TileEntity16.getBlockData(tile), tag);
        } else {
            load.invoke(tile, tag);
        }
    }

    private static final class TileEntity16 {

        private static final MethodHandle getPosition;
        private static final MethodHandle getWorld;
        private static final MethodHandle getType;

        static {
            MethodHandle m1 = null, m2 = null, m3 = null;
            if (ServerInstance.verNumber == 16) {
                try {
                    m1 = EasyLookup.method("TileEntity", "getPosition", "BlockPosition");
                    m2 = EasyLookup.method("TileEntity", "getWorld", "World");
                    m3 = EasyLookup.method("World", "getType", void.class, "IBlockData", "BlockPosition");
                } catch (NoSuchMethodException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            getPosition = m1;
            getWorld = m2;
            getType = m3;
        }

        public static Object getBlockData(Object tile) throws Throwable {
            return getType.invoke(getWorld.invoke(tile), getPosition.invoke(tile));
        }
    }
}
