
package tk.xenon98.laundryapp.driver.laundry;

import static tk.xenon98.laundryapp.common.utils.Utils.waitUntil;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import org.apache.logging.log4j.util.TriConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.xenon98.laundryapp.common.utils.Utils;
import tk.xenon98.laundryapp.common.utils.graph.GraphUtils;
import tk.xenon98.laundryapp.driver.IAppDriver;
import tk.xenon98.laundryapp.driver.IDriver;
import tk.xenon98.laundryapp.driver.NexusLauncherAppDriver;
import tk.xenon98.laundryapp.driver.UiDriver;
import tk.xenon98.laundryapp.driver.laundry.ActivityWithMenu.MenuItem;
import tk.xenon98.laundryapp.driver.xml.Hierarchy;
import tk.xenon98.laundryapp.driver.xml.HierarchyUtil;
import tk.xenon98.laundryapp.driver.xml.Node;

public class LaundryAppDriver implements IDriver, IAppDriver {

	private static final Logger LOG = LoggerFactory.getLogger(LaundryAppDriver.class);
	@Getter
	private final UiDriver uiDriver;
	@Getter
	private final NexusLauncherAppDriver launcherAppDriver;
	@Getter(AccessLevel.MODULE)
	private final String email;
	@Getter(AccessLevel.MODULE)
	private final String password;

	private final Map<Class<? extends IActivity>, IActivity> initializedActivities = new HashMap<>();

	public static final String PACKAGE_NAME = "com.innovationscript.lalaunderette";

	public LaundryAppDriver(final UiDriver uiDriver, final NexusLauncherAppDriver launcherAppDriver,
			final String email, final String password) {
		this.uiDriver = uiDriver;
		this.launcherAppDriver = launcherAppDriver;
		this.email = email;
		this.password = password;
	}

	@Override
	public String getPackageName() {
		return PACKAGE_NAME;
	}

	@Override
	public @NonNull Map<String, String> getUiStateAttributes(final String activityName,
			final Hierarchy hierarchy) {
		return Map.of();
	}

	@Override
	public void launchApp() throws IOException, ExecutionException, InterruptedException {
		uiDriver.launchApp(PACKAGE_NAME);
	}

	public <T extends IActivity> T launchActivity(Class<T> activityClass)
			throws IOException, ExecutionException, InterruptedException {
		ensureAppIsLaunched();

		final LaundryAppState currentState = getCurrentState();
		final List<LaundryAppState> path =
				findPath(currentState, LaundryAppState.findByActivityClass(activityClass));
		Utils.slidingWindow(path.stream(), 2, new LaundryAppState[0]).forEach(transition -> {
			final LaundryAppState from = transition[0];
			final LaundryAppState to = transition[1];
			try {
				findTransition(from, to).run();
			} catch (InterruptedException | ExecutionException | IOException e) {
				throw new RuntimeException(e);
			}
		});

		return initializedActivity(activityClass);
	}

	private <T extends IActivity> T initializedActivity(final Class<T> activityClass) {
		return (T) this.initializedActivities.computeIfAbsent(activityClass, this::createActivity);
	}

	private IActivity createActivity(final Class<? extends IActivity> activityClass) {
		final Constructor<?> constructor = Arrays.stream(activityClass.getConstructors())
				.filter(c -> c.getParameterCount() == 1
						&& c.getParameterTypes()[0].isAssignableFrom(LaundryAppDriver.class))
				.findFirst()
				.orElseThrow();

		try {
			final var activity = (IActivity) constructor.newInstance(this);
			activity.init();
			return activity;
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private List<LaundryAppState> findPath(final LaundryAppState beginState, final LaundryAppState endState) {
		return GraphUtils.findPath(LaundryAppActivityGraph.INSTANCE, beginState, endState);
	}

	public LaundryAppState getCurrentState() throws IOException, ExecutionException, InterruptedException {
		return LaundryAppState.findByActivityName(uiDriver.focusedActivity());
	}

	private void ensureAppIsLaunched() throws IOException, ExecutionException, InterruptedException {
		if (!appIsLaunched()) {
			launchApp();
			try {
				waitUntil(() -> {
					try {
						return appIsLaunched();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}, "Laundry app is launched", Duration.ofSeconds(30), Duration.ofMillis(250));
			} catch (TimeoutException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private boolean appIsLaunched() throws IOException, ExecutionException, InterruptedException {
		return PACKAGE_NAME.equals(uiDriver.focusedWindowPackage());
	}

	public String reserveWasher() throws IOException, ExecutionException, InterruptedException {
		return reserveWasher(LOG::info);
	}

	public String reserveWasher(final Consumer<String> statusConsumer) throws IOException, ExecutionException,
			InterruptedException {
		ensureAppIsLaunched();

		ReserveWasherActivity activity = launchActivity(ReserveWasherActivity.class);
		while (true) {
			final Collection<MachineItem> machines = activity.scanAllItems();

			final var reservedForYou = machines.stream().filter(machine -> machine.status()
					== MachineStatus.RESERVED_FOR_YOU).findFirst();
			if (reservedForYou.isPresent()) {
				final String name = reservedForYou.get().name();
				statusConsumer.accept("Found previously reserved machine: " + name);
				return name;
			}

			final var selectable = machines.stream().filter(machine -> machine.status() == MachineStatus.SELECT)
					.findFirst();

			if (selectable.isPresent()) {
				final String name = selectable.get().name();
				statusConsumer.accept("Found available machine, trying to reserve: " + name);

				final Node itemNode = Objects.requireNonNull(activity.scrollToItem(name));
				uiDriver.tap(itemNode);

				try {
					waitUntil(() -> {
						try {
							return HierarchyUtil.findByResourceId(uiDriver.getUiHierarchy(),
									"com.innovationscript.lalaunderette:id/button_no") != null;
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}, "Reservation confirmation", Duration.ofSeconds(10), Duration.ofMillis(250));

					final Node noButton = HierarchyUtil.findByResourceId(uiDriver.getUiHierarchy(),
							"com.innovationscript.lalaunderette:id/button_no");
					uiDriver.tap(noButton);
					statusConsumer.accept("Reserved successfully: " + name);
					return name;
				} catch (TimeoutException e) {
					// Do nothing
					statusConsumer.accept("Reservation confirmation failed");
				}
			} else {
				final TriConsumer<Map<MachineStatus, Duration>, MachineStatus, Duration>
						acceptMachine = (map, status, timeLeft) -> {
					if (map.containsKey(status)) {
						final Duration oldTimeLeft = map.get(status);
						if (oldTimeLeft.compareTo(timeLeft) < 0) {
							timeLeft = oldTimeLeft;
						}
					}
					map.put(status, timeLeft);
				};

				final var timeLeftByStatus =
						machines.stream().filter(item -> item.timeLeft().isPresent())
								.reduce(new EnumMap<MachineStatus, Duration>(MachineStatus.class), (map, item) -> {
									acceptMachine.accept(map, item.status(), item.timeLeft().get());
									return map;
								}, (leftMap, rightMap) -> {
									rightMap.forEach((k, v) -> acceptMachine.accept(leftMap, k, v));
									return leftMap;
								});

				final var sb = new StringBuilder();
				sb.append("No ready machines available.\nPending machines:");
				timeLeftByStatus.forEach((status, timeLeft) -> {
					final long secondsLeft = timeLeft.getSeconds();
					sb.append('\n').append(status.name()).append(": ").append(String.format("%02d:%02d", secondsLeft / 60, secondsLeft % 60));
				});
				sb.append("\nRefreshing...");
				statusConsumer.accept(sb.toString());
			}

			// Do refresh
			launchActivity(HomeActivity.class);
			activity = launchActivity(ReserveWasherActivity.class);
		}
	}

	private void login() throws IOException, ExecutionException, InterruptedException {
		final var activity = initializedActivity(LoginActivity.class);
		activity.enterEmail(email);
		activity.enterPassword(password);
		activity.signIn();
	}

	@FunctionalInterface
	private interface Transition {

		void run() throws InterruptedException, ExecutionException, IOException;
	}

	private Transition findTransition(final LaundryAppState from, final LaundryAppState to) {
		if (!LaundryAppActivityGraph.INSTANCE.outgoingNeighbours(from).contains(to)) {
			throw new IllegalArgumentException("No edge between " + from + " and " + to);
		}

		if (to == LaundryAppState.SHUTDOWN) {
			return this::transitionToShutdown;
		} else if (to == LaundryAppState.MENU) {
			return this::transitionToMenu;
		} else {
			switch (from) {
				case SHUTDOWN -> {
					return this::launchApp;
				}
				case MENU -> {
					return () -> transitionFromMenu(to);
				}
				case START_ACTIVITY, LOGIN_ACTIVITY -> {
					if (to != LaundryAppState.HOME_ACTIVITY) {
						throw new IllegalStateException();
					}
					return this::transitionStartHome;
				}
				case HOME_ACTIVITY -> {
					switch (to) {
						case SELECT_WASHER -> {
							return () -> initializedActivity(HomeActivity.class).clickSelectWasher();
						}
						case SELECT_DRYER -> {
							return () -> initializedActivity(HomeActivity.class).clickSelectDryer();
						}
						case RESERVE_WASHER -> {
							return () -> initializedActivity(HomeActivity.class).clickReserveWasher();
						}
						default -> throw new IllegalStateException();
					}
				}
				default -> throw new IllegalStateException("Unknown transition: " + from + " -> " + to);
			}
		}
	}

	private void transitionFromMenu(final LaundryAppState to)
			throws IOException, ExecutionException, InterruptedException {
		final var menu = initializedActivity(ActivityWithMenu.class);
		menu.openMenu();
		menu.selectItem(MenuItem.findByActivity(to));

		if (to == LaundryAppState.HOME_ACTIVITY) {
			while (true) {
				try {
					waitUntil(() -> initializedActivity(HomeActivity.class).isReady(), "Home activity is ready",
							Duration.ofSeconds(10), Duration.ofMillis(250));
					break;
				} catch (TimeoutException e) {
					menu.openMenu();
					menu.selectItem(MenuItem.findByActivity(to));
				}
			}
		}
	}

	private void transitionToMenu() throws IOException, ExecutionException, InterruptedException {
		initializedActivity(ActivityWithMenu.class).openMenu();
	}

	private void transitionToShutdown() throws IOException, ExecutionException, InterruptedException {
		launcherAppDriver.killAllTasks();
	}

	private void transitionStartHome() throws InterruptedException, IOException, ExecutionException {
		try {
			waitUntil(() -> {
				try {
					return !uiDriver.focusedActivity().equals(LaundryAppState.START_ACTIVITY.getActivityName());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}, "App is loaded", Duration.ofSeconds(30), Duration.ofMillis(250));
		} catch (TimeoutException e) {
			throw new RuntimeException(e);
		}

		if (uiDriver.focusedActivity().equals(LaundryAppState.LOGIN_ACTIVITY.getActivityName())) {
			login();
		}
	}

}
