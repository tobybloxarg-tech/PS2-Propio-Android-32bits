package com.virtualapplications.play.settings;

import android.os.Bundle;

import com.virtualapplications.play.R;
import com.virtualapplications.play.SettingsManager;

import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceCategory;
import androidx.preference.SwitchPreferenceCompat;

public class EmulatorSettingsFragment extends PreferenceFragmentCompat
{
	private ListPreference performancePreset;
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
	{
		addPreferencesFromResource(R.xml.preferences_emu);
		addPerformancePresetPreference();
		writeToPreferences(getPreferenceScreen());
	}

	@Override
	public void onDestroy()
	{
		readFromPreferences(getPreferenceScreen());
		super.onDestroy();
	}

	private void addPerformancePresetPreference()
	{
		PreferenceCategory category = new PreferenceCategory(requireContext());
		category.setTitle("Optimización rápida");
		category.setIconSpaceReserved(false);
		getPreferenceScreen().addPreference(category);

		performancePreset = new ListPreference(requireContext());
		performancePreset.setKey("ui.performancepreset");
		performancePreset.setPersistent(false);
		performancePreset.setTitle("Perfil de rendimiento");
		performancePreset.setDialogTitle("Elegí un perfil");
		performancePreset.setSummary("Manual, compatible, liviano sin audio o prueba Vulkan. Ningún perfil garantiza más FPS.");
		performancePreset.setEntries(new CharSequence[] {"Manual (no cambiar)", "Compatible: OpenGL 1×", "Liviano: OpenGL 1×, sin audio", "Prueba: Vulkan 1×"});
		performancePreset.setEntryValues(new CharSequence[] {"0", "1", "2", "3"});
		performancePreset.setDefaultValue("0");
		performancePreset.setValue("0");
		performancePreset.setOnPreferenceChangeListener((preference, newValue) -> {
			applyPerformancePreset(Integer.parseInt((String)newValue));
			return true;
		});
		performancePreset.setIconSpaceReserved(false);
		category.addPreference(performancePreset);
	}

	private void applyPerformancePreset(int preset)
	{
		if(preset == 0) return;
		ListPreference renderer = findPreference("video.gshandler");
		ListPreference resolution = findPreference("renderer.opengl.resfactor");
		SwitchPreferenceCompat bilinear = findPreference("renderer.opengl.forcebilineartextures");
		SwitchPreferenceCompat audio = findPreference("audio.enableoutput");
		SwitchPreferenceCompat fps = findPreference("ui.showfps");
		if(renderer != null) renderer.setValue(preset == 3 ? "1" : "0");
		if(resolution != null) resolution.setValue("1");
		if(bilinear != null) bilinear.setChecked(false);
		if(audio != null) audio.setChecked(preset != 2);
		if(fps != null) fps.setChecked(true);
	}

	private void readFromPreferences(final PreferenceGroup prefGroup)
	{
		for(int i = 0; i < prefGroup.getPreferenceCount(); i++)
		{
			final Preference pref = prefGroup.getPreference(i);
			if(pref instanceof SwitchPreferenceCompat)
			{
				final SwitchPreferenceCompat switchPref = (SwitchPreferenceCompat)pref;
				SettingsManager.setPreferenceBoolean(switchPref.getKey(), switchPref.isChecked());
			}
			else if(pref instanceof ListPreference)
			{
				final ListPreference listBoxPref = (ListPreference)pref;
				if(listBoxPref == performancePreset) continue;
				final int val = Integer.parseInt(listBoxPref.getValue());
				SettingsManager.setPreferenceInteger(listBoxPref.getKey(), val);
			}
			else if(pref instanceof PreferenceGroup)
			{
				readFromPreferences((PreferenceGroup)pref);
			}
		}
	}

	private void writeToPreferences(final PreferenceGroup prefGroup)
	{
		for(int i = 0; i < prefGroup.getPreferenceCount(); i++)
		{
			final Preference pref = prefGroup.getPreference(i);
			if(pref instanceof SwitchPreferenceCompat)
			{
				final SwitchPreferenceCompat switchPref = (SwitchPreferenceCompat)pref;
				switchPref.setChecked(SettingsManager.getPreferenceBoolean(switchPref.getKey()));
			}
			else if(pref instanceof ListPreference)
			{
				final ListPreference listBoxPref = (ListPreference)pref;
				if(listBoxPref == performancePreset) continue;
				final String val = String.valueOf(SettingsManager.getPreferenceInteger(listBoxPref.getKey()));
				listBoxPref.setValue(val);
			}
			else if(pref instanceof PreferenceGroup)
			{
				writeToPreferences((PreferenceGroup)pref);
			}
		}
	}
}
