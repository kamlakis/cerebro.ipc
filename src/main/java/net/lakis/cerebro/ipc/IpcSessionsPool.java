package net.lakis.cerebro.ipc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.lakis.cerebro.ipc.config.IpcConfig;
import net.lakis.cerebro.ipc.processors.IpmProcessor;
import net.lakis.cerebro.ipc.socket.SocketFactory;

@Accessors(fluent = true, chain = true)
@Getter
public abstract class IpcSessionsPool implements IpcSessionStateListener {
	private Map<String, IpcSession> sessionsMap;
	private Set<IpcSession> sessionsSet;
	private Set<IpmProcessor> ipmProcessors;
	private volatile IpcSessionState state = IpcSessionState.CLOSED;
	private @Getter IpcConfig config;
	private List<IpcSessionStateListener> sessionStateListeners;

	public IpcSessionsPool(IpcConfig config) {
		this.config = config;

		this.sessionsMap = new HashMap<>();
		this.sessionsSet = new LinkedHashSet<>();

		this.sessionStateListeners = new ArrayList<IpcSessionStateListener>();
		this.sessionStateListeners.add(this);

		this.ipmProcessors = new HashSet<IpmProcessor>();

//		this.ipmProcessors.put(UnbindIpm.TAG, UnbindProcessor.INSTANCE);
	}

	public IpcSession createSession(SocketFactory socketFactory) {

		return new IpcSession(this, config, ipmProcessors, sessionStateListeners, socketFactory);
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

	public void addIpmProcessor(IpmProcessor ipmProcessor) {
		this.ipmProcessors.add(ipmProcessor);
	}

	public void removeIpmProcessor(IpmProcessor ipmProcessor) {
		this.ipmProcessors.remove(ipmProcessor);
	}

	public void ipmProcessor(IpmProcessor ipmProcessor) {
		this.ipmProcessors.clear();
		this.ipmProcessors.add(ipmProcessor);
	}

	public String sizeReport() {
		StringBuilder sb = new StringBuilder();

		boolean first = true;
		for (IpcSession s : sessionsSet) {
			if (first) {
				first = false;
			} else {
				sb.append(",");
			}
			sb.append(s.sizeReport());
		}
		return sb.toString();
	}
}
