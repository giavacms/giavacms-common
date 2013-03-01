/*
 * Copyright 2013 GiavaCms.org.
 *
 * Licensed under the Eclipse Public License version 1.0, available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.giavacms.common.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

@Named
@Singleton
@ApplicationScoped
public class ModuleRegistry {

	private Map<String, ModuleProvider> modules = new HashMap<String, ModuleProvider>();

	@Inject
	Instance<ModuleProvider> moduleProviders;

	@PostConstruct
	public void postConstruct() {
		for (ModuleProvider provider : moduleProviders) {
			modules.put(provider.getName(), provider);
		}
	}

	@Named
	@Produces
	public List<ModuleProvider> getModules() {
		// LA HASH MAP FA GIA' SORT PER CHIAVE DA SE'
		List<ModuleProvider> list = new ArrayList<ModuleProvider>(
				modules.values());
		// return new ArrayList<ModuleProvider>(modules.values());
		Collections.sort(list, new Comparator<ModuleProvider>() {
			@Override
			public int compare(ModuleProvider o1, ModuleProvider o2) {

				if (o1.getPriority() > o2.getPriority())
					return 1;
				else if (o1.getPriority() < o2.getPriority())
					return -1;
				else
					return 0;
			}
		});
		return list;

	}

	public Map<String, String> getPermissions() {
		Map<String, String> map = new HashMap<String, String>();
		for (ModuleProvider module : getModules()) {
			Map<String, String> module_map = module.getPermissions();
			for (String key : module_map.keySet()) {
				map.put(key, module_map.get(key));
			}
		}
		return map;
	}

	public List<String> getAllowableOperations() {
		List<String> list = new ArrayList<String>();
		for (ModuleProvider module : getModules()) {
			list.addAll(module.getAllowableOperations());
		}
		return list;
	}

	@Named
	@Produces
	public SelectItem[] getRuoliItems() {
		List<ModuleProvider> moduli = getModules();
		if (moduli != null) {
			List<SelectItem> ruoliItems = new ArrayList<SelectItem>();
			for (ModuleProvider module : getModules()) {
				Map<String, String> module_map = module.getPermissions();
				if (module_map != null && module_map.size() > 0) {
					for (String key : module_map.keySet()) {
						String value = module_map.get(key);
						ruoliItems.add(new SelectItem(key, module.getName()
								+ "-" + value));
					}
				}
			}
			return ruoliItems.toArray(new SelectItem[] {});
		}
		return new SelectItem[] {};
	}

	@Named
	@Produces
	public SelectItem[] getRuoliItemsWithAdminAndAll() {
		List<ModuleProvider> moduli = getModules();
		if (moduli != null) {
			List<SelectItem> ruoliItems = new ArrayList<SelectItem>();
			ruoliItems.add(new SelectItem(null, "ruolo"));
			ruoliItems.add(new SelectItem("admin", "admin"));
			for (ModuleProvider module : getModules()) {
				Map<String, String> module_map = module.getPermissions();
				if (module_map != null && module_map.size() > 0) {
					for (String key : module_map.keySet()) {
						String value = module_map.get(key);
						ruoliItems.add(new SelectItem(key, module.getName()
								+ "-" + value));
					}
				}
			}
			return ruoliItems.toArray(new SelectItem[] {});
		}
		return new SelectItem[] {};
	}

	@Named
	@Produces
	public List<String> getExtensions() {
		List<String> extensions = new ArrayList<String>();
		for (ModuleProvider moduleProvider : modules.values()) {
			if (moduleProvider instanceof ExtensionProvider) {
				extensions.addAll(((ExtensionProvider) moduleProvider)
						.getExtensions());
			}
		}
		return extensions;
	}
}
