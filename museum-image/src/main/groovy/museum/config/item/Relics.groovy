package museum.config.item

import static clepto.bukkit.item.Items.register
import static org.bukkit.Material.CLAY_BALL

register 'relic-totem', {
    item CLAY_BALL
    nbt.price = 14
    nbt.relic = 'totem'
    nbt.museum = 'totem'
    nbt.glow_color = 0x50008000
    text """&e⭐⭐⭐ &fТотем Майя

    &eДоход оценивается в 14\$
    
    Можно продать перекупщику 

    &7Поставьте реликвию на витрину для
    &7реликвий, которую вы можете купить
    &7в магазине построек.
    """
}

register 'relic-shap', {
    item CLAY_BALL
    nbt.price = 8
    nbt.relic = 'shap'
    nbt.museum = 'shap'
    nbt.glow_color = 0x50808080
    text """&e⭐ &fМаска война Майя

    &eДоход оценивается в 8\$
    
    Можно продать перекупщику 

    &7Поставьте реликвию на витрину для
    &7реликвий, которую вы можете купить
    &7в магазине построек.
    """
}

register 'relic-yantar', {
    item CLAY_BALL
    nbt.price = 20
    nbt.relic = 'yantar'
    nbt.museum = 'yantar'
    nbt.glow_color = 0x50000080
    text """&e⭐⭐⭐ &fЯнтарь

    &eДоход оценивается в 20\$
    
    Можно продать перекупщику 

    &7Поставьте реликвию на витрину для
    &7реликвий, которую вы можете купить
    &7в магазине построек.
    """
}


register 'relic-tooth', {
    item CLAY_BALL
    nbt.price = 9
    nbt.relic = 'tooth'
    nbt.museum = 'tooth'
    nbt.glow_color = 0x50808080
    text """&e⭐ &fЗуб мегалодона

    &eДоход оценивается в 9\$
        
    Можно продать перекупщику 

    &7Поставьте реликвию на витрину для
    &7реликвий, которую вы можете купить
    &7в магазине построек.
    """
}

register 'relic-anubis', {
    item CLAY_BALL
    nbt.price = 12
    nbt.relic = 'anubis'
    nbt.museum = 'anubisstick'
    nbt.glow_color = 0x50008000
    text """&e⭐⭐ &fОрудие Анубиса

    &eДоход оценивается в 12\$
        
    Можно продать перекупщику 

    &7Поставьте реликвию на витрину для
    &7реликвий, которую вы можете купить
    &7в магазине построек.
    """
}

register 'relic-pot', {
    item CLAY_BALL
    nbt.price = 7
    nbt.relic = 'pot'
    nbt.museum = 'pot'
    nbt.glow_color = 0x50808080
    text """&e⭐ &fДревний горшок

    &eДоход оценивается в 7\$
        
    Можно продать перекупщику 

    &7Поставьте реликвию на витрину для
    &7реликвий, которую вы можете купить
    &7в магазине построек.
    """
}

register 'relic-shield', {
    item CLAY_BALL
    nbt.price = 10
    nbt.relic = 'shield'
    nbt.museum = 'shieldofruin'
    nbt.glow_color = 0x50008000
    text """&e⭐⭐ &fЩит война Древнего Египта

    &eДоход оценивается в 10\$
        
    Можно продать перекупщику 

    &7Поставьте реликвию на витрину для
    &7реликвий, которую вы можете купить
    &7в магазине построек.
    """
}