package com.gitlab.jeeto.oboco.client;

import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MockSharedPreferences implements SharedPreferences {

    public final HashMap<String, Object> preferenceMap;
    private final MockSharedPreferencesEditor preferenceEditor;

    public MockSharedPreferences() {
        preferenceMap = new HashMap<>();
        preferenceEditor = new MockSharedPreferencesEditor(preferenceMap);
    }

    @Override
    public Map<String, ?> getAll() {
        return preferenceMap;
    }

    @Override
    public String getString(final String s, final String s1) {
        return (String) preferenceMap.get(s);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<String> getStringSet(final String s, final Set<String> set) {
        return (Set<String>) preferenceMap.get(s);
    }

    @Override
    public int getInt(final String s, final int i) {
        return (int) preferenceMap.get(s);
    }

    @Override
    public long getLong(final String s, final long l) {
        return (long) preferenceMap.get(s);
    }

    @Override
    public float getFloat(final String s, final float v) {
        return (float) preferenceMap.get(s);
    }

    @Override
    public boolean getBoolean(final String s, final boolean b) {
        return (boolean) preferenceMap.get(s);
    }

    @Override
    public boolean contains(final String s) {
        return preferenceMap.containsKey(s);
    }

    @Override
    public Editor edit() {
        return preferenceEditor;
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {

    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(final OnSharedPreferenceChangeListener onSharedPreferenceChangeListener) {

    }

    public static class MockSharedPreferencesEditor implements Editor {

        private final HashMap<String, Object> preferenceMap;

        public MockSharedPreferencesEditor(final HashMap<String, Object> preferenceMap) {
            this.preferenceMap = preferenceMap;
        }

        @Override
        public Editor putString(final String s, final String s1) {
            preferenceMap.put(s, s1);
            return this;
        }

        @Override
        public Editor putStringSet(final String s, final Set<String> set) {
            preferenceMap.put(s, set);
            return this;
        }

        @Override
        public Editor putInt(final String s, final int i) {
            preferenceMap.put(s, i);
            return this;
        }

        @Override
        public Editor putLong(final String s, final long l) {
            preferenceMap.put(s, l);
            return this;
        }

        @Override
        public Editor putFloat(final String s, final float v) {
            preferenceMap.put(s, v);
            return this;
        }

        @Override
        public Editor putBoolean(final String s, final boolean b) {
            preferenceMap.put(s, b);
            return this;
        }

        @Override
        public Editor remove(final String s) {
            preferenceMap.remove(s);
            return this;
        }

        @Override
        public Editor clear() {
            preferenceMap.clear();
            return this;
        }

        @Override
        public boolean commit() {
            return true;
        }

        @Override
        public void apply() {
            // Nothing to do, everything is saved in memory.
        }
    }

}
