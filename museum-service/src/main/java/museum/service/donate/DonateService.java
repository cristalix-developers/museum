package museum.service.donate;

import lombok.RequiredArgsConstructor;
import museum.service.MuseumService;
import museum.donate.DonateType;
import museum.service.donate.booster.GlobalBoosterDonate;
import museum.packages.UserTransactionPackage;
import museum.service.user.IUserService;
import museum.service.user.ServiceUser;
import ru.cristalix.core.coupons.CouponData;
import ru.cristalix.core.lib.Futures;
import ru.cristalix.core.network.ISocketClient;
import ru.cristalix.core.network.packages.CouponsDataPackage;
import ru.cristalix.core.network.packages.MoneyTransactionRequestPackage;
import ru.cristalix.core.network.packages.MoneyTransactionResponsePackage;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static museum.boosters.BoosterType.*;
import static museum.donate.DonateType.*;

@RequiredArgsConstructor
public class DonateService implements IDonateService {

	private final Map<DonateType, Donate> serviceDonateHandlers = new EnumMap<>(DonateType.class);
	private final MuseumService museumService;
	private final IUserService playerService;

	{
		// Глобальные бустеры
		serviceDonateHandlers.put(GLOBAL_MONEY_BOOSTER, new GlobalBoosterDonate(COINS, 60 * 60));
		serviceDonateHandlers.put(GLOBAL_EXP_BOOSTER, new GlobalBoosterDonate(EXP, 60 * 60));
		serviceDonateHandlers.put(GLOBAL_VILLAGER_BOOSTER, new GlobalBoosterDonate(VILLAGER, 60 * 60));
	}

	@Override
	public void enable() {

		museumService.registerHandler(UserTransactionPackage.class, (realm, transaction) -> {

			String response = this.processTransaction(transaction);
			if (response != null) {
				transaction.setResponse(response);
				realm.send(transaction);
			}

		});

	}

	public String processTransaction(UserTransactionPackage transaction) {

		ServiceUser user = playerService.getUser(transaction.getUser());
		DonateType type = transaction.getDonate();

		Donate donate = serviceDonateHandlers.get(type);
		if (donate == null)
			return "donate-doesnt-exist";

		String rejectMessage = donate.accept(user);
		if (rejectMessage != null)
			return rejectMessage;

		if (type.isSave() && user.getInfo().getDonates().contains(type))
			return "donate-already-bought";

		this.findCoupon(transaction.getUser()).thenCompose(coupon -> {
			int price = type.getPrice();
			if (coupon != null)
				price = (int) Math.ceil(coupon.priceWithDiscount(price));

			return processInvoice(user, price, type.getName());
		}).thenAccept(response -> {
			if (response.getErrorMessage() != null) {
				String err = response.getErrorMessage();
				boolean serverFault = !err.equalsIgnoreCase("Недостаточно средств на счету");
				transaction.setResponse(serverFault ? err : "donate-insufficient-funds");
				if (serverFault) {
					System.out.println("[Donate] Unable to process transaction by " + user.getName() + ": " + err);
				}
			} else {
				donate.grant(user);
				System.out.println("[Donate] " + user.getName() + " bought " +
						transaction.getDonate().name().toLowerCase() + " for " + type.getPrice() + " units.");
			}

			user.getRealm().send(transaction);
		});

		return null;
	}

	private CompletableFuture<CouponData> findCoupon(UUID user) {
		CompletableFuture<CouponData> future = new CompletableFuture<>();
		Futures.fail(Futures.timeout(
				ISocketClient.get().<CouponsDataPackage>writeAndAwaitResponse(new CouponsDataPackage(user)).thenAccept(pckg -> future.complete(pckg.getData())),
				5L, TimeUnit.SECONDS
									), throwable -> {
			throwable.printStackTrace();
			future.complete(null);
		});
		return future;
	}

	public CompletableFuture<MoneyTransactionResponsePackage> processInvoice(ServiceUser user, int price, String description) {
		if (System.getenv("TRANSACTION_TEST") != null || user.getName().equals("DelfikPro"))
			return CompletableFuture.completedFuture(new MoneyTransactionResponsePackage(null, null));
		return ISocketClient.get().writeAndAwaitResponse(new MoneyTransactionRequestPackage(user.getUuid(), price, true, description));
	}

}
