package net.lakis.cerebro.ipc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.lakis.cerebro.ipc.config.IpcConfig;
import net.lakis.cerebro.ipc.ipm.IpmData;
import net.lakis.cerebro.socket.SocketFactory;

@Accessors(fluent = true, chain = true)
@Getter
public abstract class IpcSessionsPool implements IpcSessionStateListener {
	private Map<String, IpcSession> sessionsMap;
	private Set<IpcSession> sessionsSet;
	private volatile IpcSessionState state = IpcSessionState.CLOSED;
	private IpcConfig config;
	private List<IpcSessionStateListener> sessionStateListeners;
	private @Setter Consumer<IpmData> ipmDataHandler;

	public IpcSessionsPool(IpcConfig config) {
		this.config = config;

		this.sessionsMap = new HashMap<>();
		this.sessionsSet = new LinkedHashSet<>();

		this.sessionStateListeners = new ArrayList<IpcSessionStateListener>();
		this.sessionStateListeners.add(this);
	}

	public IpcSession createSession(SocketFactory socketFactory) {
		return new IpcSession(this, config, sessionStateListeners, socketFactory);
	}

	public IpcSession getSessionById(String id) {
		return sessionsMap.get(id);
	}

	public Collection<IpcSession> getBoundSessions() {
		return sessionsMap.values();
	}

	public void removeBoundSession(IpcSession session) {
		if (session != null)
			sessionsMap.values().remove(session);
	}

	public void add(IpcSession session) {
		sessionsSet.add(session);
	}

	public Collection<IpcSession> getAllSessions() {
		return sessionsSet;
	}

	protected void setState(IpcSessionState state) {
		this.state = state;
	}

	public void addSessionStateListener(IpcSessionStateListener l) {
		sessionStateListeners.add(l);
	}

	public void removeSessionStateListener(IpcSessionStateListener l) {
		sessionStateListeners.remove(l);
	}
	 

}
