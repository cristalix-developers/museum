package museum.config.item

import museum.museum.subject.skeleton.Skeleton

import static clepto.bukkit.item.Items.register
import static org.bukkit.Material.BONE

register 'skeleton', {
    item BONE
    def skeleton = (Skeleton) context
    text "§f» Скелет §b$skeleton.prototype.title §f«"
    text '§eФрагменты: §f' + skeleton.unlockedFragments.size() + '§f/§e' + skeleton.prototype.fragments.size()
    text '§eОткуда: §f' + skeleton.prototype.rarity.period
    text '§eРазмер: §f' + skeleton.prototype.size + 'x' + skeleton.prototype.size

}