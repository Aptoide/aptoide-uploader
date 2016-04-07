/*
 * Copyright (c) 2016.
 * Modified by Neurophobic Animal on 07/04/2016.
 */

package pt.caixamagica.aptoide.uploader.webservices.json;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by neuro on 16-02-2015.
 */
public class CategoriesJson {

	@Getter @Setter public String status;
	@Getter @Setter public Categories categories;
	@Getter @Setter List<Error> errors;

	public static class Categories {

		@Getter @Setter public List<Category> standard;
		@Getter @Setter public List<Category> custom;
	}

	public static class Category {

		@Getter @Setter public Number id;
		@Getter @Setter public Number parent;
		@Getter @Setter public String name;

		public Category() {
		}

		public Category(int i) {
			id = i;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}

			if (o instanceof Category) {
				return ((Category) o).getId().equals(id);
			}

			return super.equals(o);
		}
	}
}