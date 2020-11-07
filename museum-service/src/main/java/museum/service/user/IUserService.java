package museum.service.user;

import museum.packages.TopPackage;
import museum.tops.PlayerTopEntry;
import ru.cristalix.core.IService;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface IUserService extends IService {

	CompletableFuture<List<PlayerTopEntry<Object>>> generateTop(TopPackage.TopType topType, int limit);

	ServiceUser getUser(UUID uuid);

	Collection<? extends ServiceUser> getUsers();

}
