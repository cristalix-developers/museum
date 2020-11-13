@groovy.transform.BaseScript(museum.MuseumScript)
package museum.config.command

import clepto.bukkit.menu.Guis

import static org.bukkit.Material.PAPER

registerCommand 'helps' handle {
    Guis.open player, 'helps', player
}

Guis.register 'helps', {
    title 'Часто задаваемые вопросы'
    layout """
        XX--X--X-
        X-X-X-X-X
        -X--X--X-
        """
    button 'X' icon {
        item PAPER
        text """&bЧто нового?

        Реликвии и витрины артефактов.
        Раскопки руин в Египте.
        Раскопки во время реставрации Рима.
        Новые динозавры.
        Добавлены новые места.
        Добавлен этот самый помощник!
        """
    }
    button 'X' icon {
        item PAPER
        text """&bЧто нужно делать в режиме Музей?

        Посещать экспедиции, вскапывать
        нужные блоки, получать опыт и
        в последствии повышать уровень.
        Выставлять скелеты на витрины и
        повышать свой заработок.
        """
    }

    button 'X' icon {
        item PAPER
        text """&bКак посещать экспедиции?

        Нужно зайти в "Меню" (Листок, 
        1 слот в твоём инвентаре), нажать 
        на "Экспедиции" (Компас) и выбрать
        нужную локацию из списка.
        """
    }

    button 'X' icon {
        item PAPER
        text """&bКак улучшить кирку?

        Для этого нужно нажать "Меню" 
        (1 слот инвентаря), а затем 
        на меню "Кирки" (Золотая кирка).
        Здесь ты сможешь улучшить её.
        Всего 4 уровня кирок: Стандартная,
        Профессиональная за 30.000\$, 
        Престижная за 200.000\$ ,
        Легендарная за 5.000.000\$.
        """
    }

    button 'X' icon {
        item PAPER
        text """&bКак пользоваться лавкой?

        Для закупки товаров нужно
        зайти в "Меню" (Листок),
        Товар (Правый верхний угол,
        вагонетка с сундуком).
        Дальше подойти к нпс Олегу (290 -400),
        забрать груз и дойти обратно
        до лавки. (280 -270)
        """
    }

    button 'X' icon {
        item PAPER
        text """&bМожно ли здесь играть с другом?

        Нет, это одиночный режим, 
        но твой друг может посетить
        музей, если ты находишься на
        одном сервере с ним, с помощью
        /museum visit "Ник" или же через
        меню "Посмотреть музеи" (4 слот
        инвентаря).
        """
    }

    button 'X' icon {
        item PAPER
        text """&bДает ли декор доход?

        Декор создан лишь для украшения
        обстановки твоего музея.
        За его установку ничего получить
        нельзя.
        """
    }

    button 'X' icon {
        item PAPER
        text """&bЧто за предметы с экспедиций?

        Это вещи - артефакты, 
        их можно продать, для этого
        достаточно кликнуть ПКМ по ним,
        либо же ничего не делать и после
        твоего возвращения в музей они
        автоматически продадутся.
        """
    }

    button 'X' icon {
        item PAPER
        text """&bКакие существуют способы получения денег?

        Заработок осуществляется путем
        нахождения повторяющихся фрагментов
        и продажи артефактов с раскопок,
        дохода от продаж лавки, но самым главным
        способом получения денег является сбор
        монеток, падающих с жителей, которые
        собирает коллектор.
        """
    }

    button 'X' icon {
        item PAPER
        text """&bЧто за блоки в музее?

        Алмазный и золотой блок для витрин,
        изумрудный предназначен для декора,
        лазуритный же для фонтанов,
        угольный для лавок, и железный для
        коллектора.
        """
    }

    button 'X' icon {
        item PAPER
        text """&bЧто увеличивает стоимость монеты?

        Повышает доход установка новых динозавров на
        витрины (от трёх собранных фрагментов),
        прокачка уровней витрины.
        """
    }

    button 'X' icon {
        item PAPER
        text """&bНа что мне копить и тратить деньги?

        Первые деньги ты можешь
        потратить на закупку груза 
        в лавку. Дальше лучше всего 
        потратиться на покупку 
        малых витрин.
        """
    }
}