package org.posthouse.util.impl;

import org.posthouse.util.RemoteMap;

import java.util.concurrent.ConcurrentHashMap;

public class RemoteStringConcurrentHashMap extends ConcurrentHashMap<String, String> implements RemoteMap {
}
