package com.martmists.ynet.util

import com.martmists.ynet.YNetMod
import com.martmists.ynet.transfer.data.TransferData
import com.martmists.ynet.transfer.handler.TransferHandler
import com.martmists.ynet.transfer.proxy.TransferProxy
import com.martmists.ynet.transfer.type.Type
import com.mojang.serialization.Lifecycle
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.registry.Registry
import net.minecraft.util.registry.RegistryKey
import net.minecraft.util.registry.SimpleRegistry
import net.minecraft.world.World

object YNetRegistry {
    private val types = SimpleRegistry<Type>(RegistryKey.ofRegistry(Identifier("ynet:types")), Lifecycle.stable())
    private val handlers = SimpleRegistry<TransferHandler<*,*,*>>(RegistryKey.ofRegistry(Identifier("ynet:handlers")), Lifecycle.stable())

    fun <T: Type> register(type: T, handler: TransferHandler<T, *, *>): T {
        Registry.register(types, type.identifier, type)
        Registry.register(handlers, type.identifier, handler)
        return type;
    }

    fun getTypes(): List<Type> {
        return types.toList()
    }

    fun getType(id: Identifier): Type? {
        return types.get(id)
    }

    fun <T: Type> getHandler(type: T): TransferHandler<T, TransferData<T>, TransferProxy<T, TransferData<T>>>? {
        return handlers.get(type.identifier) as TransferHandler<T, TransferData<T>, TransferProxy<T, TransferData<T>>>?
    }
}
