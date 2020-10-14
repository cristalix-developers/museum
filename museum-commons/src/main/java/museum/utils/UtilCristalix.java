package museum.utils;

import lombok.experimental.UtilityClass;
import ru.cristalix.core.network.packages.GroupData;
import ru.cristalix.core.permissions.IGroup;
import ru.cristalix.core.permissions.IPermissionService;

@UtilityClass
public class UtilCristalix {

    public String createDisplayName(GroupData data) {
        String prefix;
        IGroup staffGroup = IPermissionService.get().getGroup(data.getPlayerGroup());
        IGroup donateGroup = IPermissionService.get().getGroup(data.getDonateGroup());
        if (staffGroup.getPrefix().isEmpty())
            prefix = prefix(donateGroup);
        else
            prefix = prefix(staffGroup);
        String color =
                data.getColor() != null ? data.getColor() :
                        !"PLAYER".equals(staffGroup.getName()) ? staffGroup.getNameColor() :
                                donateGroup.getNameColor();
        return (prefix.isEmpty() ? "" : prefix + " ") + color + data.getUsername();
    }

    private String prefix(IGroup group) {
        return group.getPrefix().isEmpty() ? "" : group.getPrefixColor() + group.getPrefix();
    }

}
