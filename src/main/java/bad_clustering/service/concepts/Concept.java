package clustering.service.concepts;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.textocat.api.sdk.model.EntityAnnotationCategory;

public class Concept {
	private Set<String> words;
	private EntityAnnotationCategory category;
	private boolean hasProper;
	private String forAttributeName = null;
	
	public Concept(EntityAnnotationCategory category) {
		this.words = new HashSet<>();
		this.category = category;
		this.hasProper = false;
	}
	
	public Concept(Collection<String> words, EntityAnnotationCategory category) {
		this.words = new HashSet<>(words);
		this.category = category;
		
		this.hasProper = false;
		for (String w : words) {
			if (forAttributeName == null || (forAttributeName.length() <= 4 && w.length() > 4)) {
				forAttributeName = w;
			}
			char c = w.charAt(0);
			if (c >= 'A' && c <= 'Z' || c >= 'А' && c <= 'Я') {
				this.hasProper = true;
				forAttributeName = w;
				break;
			}
		}
	}
	
	public boolean joinWordToConcept(String newWord, EntityAnnotationCategory eac) {
		if (!(this.category == eac)) {
			return false;
		}
		for (String s : words) {
			if (fractionalEquals(newWord, s) > 0.6) {
				words.add(newWord);
				return true;
			}
		}
		
		return false;
	}
	
	public boolean joinEntityToConcept(List<String> newWords, EntityAnnotationCategory eac) {
		if (!(this.category == eac)) {
			return false;
		}
		for (String w : newWords) {
			for (String s : words) {
				if (fractionalEquals(w, s) > 0.6) {
					cumulate(newWords.toArray(new String[newWords.size()]));
					return true;
				}
			}
		}
		return false;
	}
	
	public static double strictUnionChance(Concept c1, Concept c2) {
		double basicChance = 1.0;
		int uniques = c1.words.size() + c2.words.size(); //число уникальных слов, 
														 //изначально все из обоих концептов
		double equations = 0;   //число совпадений
		for (String s : c2.words) {
			if (c1.words.contains(s)) { //если есть одинаковые слова
				uniques--;
				equations++;
			} else {
				for (String s1 : c1.words) {  //частичные совпадаения
					equations += 0.8 * fractionalEquals(s, s1);	
				}
			}
		}
		
		return basicChance * (equations / uniques);
	}
	
	public Concept union(Concept c) {
		this.words.addAll(c.words);
		if (!this.hasProper && c.hasProper) {
			this.hasProper = true;
		}
		
		return this;
	}
	
	private void cumulate(String[] newWords) {
		if (hasProper) {
			for (String w : newWords) {
				words.add(w);
			}
		} else {
			for (String w : newWords) {
				words.add(w);
				if (!hasProper) {
					char c = w.charAt(0);
					if (c >= 'A' && c <= 'Z' || c >= 'А' && c <= 'Я') {
						hasProper = true;
					}
				}
			}
		}
	}
	
	public static double fractionalEquals(String s1, String s2) {
		if (s1.length() < 3 || s2.length() < 3) {
			return 0.0;
		}
		
		if (s1.length() < s2.length()) {
			String tmp = s1;
			s1 = s2;
			s2 = tmp;
		}
		int i = 0;
		int begin = 0;
		while (begin < s1.length() && s1.charAt(begin) != s2.charAt(0)) {
			begin++;
		}
		while ((begin + i) < s1.length() && i < s2.length() && s1.charAt(begin + i) == s2.charAt(i)) {
			i++;
		}
		
		double d = ((double)i / s1.length() + (double)i / s2.length()) / 2.0;
		return d;
	}
	
	public Set<String> getWords() {
		return words;
	}
	
	public EntityAnnotationCategory getCategory() {
		return category;
	}
	
	public boolean hasProper() {
		return hasProper;
	}
	
	public String forAttributeName() {
		return forAttributeName;
	}
	
	@Override
	public boolean equals(Object arg0) {
		if (this == arg0) {
			return true;
		}
		if (!(arg0 instanceof Concept)) {
			return false;
		}
		
		Concept c = (Concept)arg0;
		return this.words.equals(c.words) && this.category == c.category 
						&& this.hasProper == c.hasProper;
	}
}