package eu.mjdev.commons.codec.language.bm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class PhoneticEngine {

	static final class PhonemeBuilder {

		public static PhonemeBuilder empty(Languages.LanguageSet languages) {
			return new PhonemeBuilder(Collections.singleton(new Rule.Phoneme(
					"", languages)));
		}

		private final Set<Rule.Phoneme> phonemes;

		private PhonemeBuilder(Set<Rule.Phoneme> phonemes) {
			this.phonemes = phonemes;
		}

		public PhonemeBuilder append(CharSequence str) {
			Set<Rule.Phoneme> newPhonemes = new HashSet<Rule.Phoneme>();

			for (Rule.Phoneme ph : this.phonemes) {
				newPhonemes.add(ph.append(str));
			}

			return new PhonemeBuilder(newPhonemes);
		}

		public PhonemeBuilder apply(Rule.PhonemeExpr phonemeExpr) {
			Set<Rule.Phoneme> newPhonemes = new HashSet<Rule.Phoneme>();

			for (Rule.Phoneme left : this.phonemes) {
				for (Rule.Phoneme right : phonemeExpr.getPhonemes()) {
					Rule.Phoneme join = left.join(right);
					if (!join.getLanguages().isEmpty()) {
						newPhonemes.add(join);
					}
				}
			}

			return new PhonemeBuilder(newPhonemes);
		}

		public Set<Rule.Phoneme> getPhonemes() {
			return this.phonemes;
		}

		public String makeString() {
			StringBuilder sb = new StringBuilder();
			for (Rule.Phoneme ph : this.phonemes) {
				if (sb.length() > 0)
					sb.append("|");
				sb.append(ph.getPhonemeText());
			}
			return sb.toString();
		}
	}

	private static final class RulesApplication {
		private final List<Rule> finalRules;
		private final CharSequence input;

		private PhonemeBuilder phonemeBuilder;
		private int i;
		private boolean found;

		public RulesApplication(List<Rule> finalRules, CharSequence input,
				PhonemeBuilder phonemeBuilder, int i) {
			if (finalRules == null) {
				throw new NullPointerException(
						"The finalRules argument must not be null");
			}
			this.finalRules = finalRules;
			this.phonemeBuilder = phonemeBuilder;
			this.input = input;
			this.i = i;
		}

		public int getI() {
			return this.i;
		}

		public PhonemeBuilder getPhonemeBuilder() {
			return this.phonemeBuilder;
		}

		public RulesApplication invoke() {
			this.found = false;
			int patternLength = 0;
			RULES: for (Rule rule : this.finalRules) {
				String pattern = rule.getPattern();
				patternLength = pattern.length();
				if (!rule.patternAndContextMatches(this.input, this.i))
					continue RULES;
				this.phonemeBuilder = this.phonemeBuilder.apply(rule
						.getPhoneme());
				this.found = true;
				break RULES;
			}
			if (!this.found)
				patternLength = 1;
			this.i += patternLength;
			return this;
		}

		public boolean isFound() {
			return this.found;
		}
	}

	private static final Map<NameType, Set<String>> NAME_PREFIXES = new EnumMap<NameType, Set<String>>(
			NameType.class);

	static {
		NAME_PREFIXES.put(NameType.ASHKENAZI, Collections
				.unmodifiableSet(new HashSet<String>(Arrays.asList("bar",
						"ben", "da", "de", "van", "von"))));
		NAME_PREFIXES.put(NameType.SEPHARDIC, Collections
				.unmodifiableSet(new HashSet<String>(Arrays.asList("al", "el",
						"da", "dal", "de", "del", "dela", "de la", "della",
						"des", "di", "do", "dos", "du", "van", "von"))));
		NAME_PREFIXES.put(NameType.GENERIC, Collections
				.unmodifiableSet(new HashSet<String>(Arrays.asList("da", "dal",
						"de", "del", "dela", "de la", "della", "des", "di",
						"do", "dos", "du", "van", "von"))));
	}

	private static CharSequence cacheSubSequence(final CharSequence cached) {
		final CharSequence[][] cache = new CharSequence[cached.length()][cached
				.length()];

		return new CharSequence() {

			public char charAt(int index) {
				return cached.charAt(index);
			}

			public int length() {
				return cached.length();
			}

			public CharSequence subSequence(int start, int end) {
				if (start == end)
					return "";
				CharSequence res = cache[start][end - 1];
				if (res == null) {
					res = cached.subSequence(start, end);
					cache[start][end - 1] = res;
				}
				return res;
			}
		};
	}

	private static String join(Iterable<String> strings, String sep) {
		StringBuilder sb = new StringBuilder();
		Iterator<String> si = strings.iterator();
		if (si.hasNext())
			sb.append(si.next());
		while (si.hasNext())
			sb.append(sep).append(si.next());
		return sb.toString();
	}

	private final Lang lang;

	private final NameType nameType;

	private final RuleType ruleType;

	private final boolean concat;

	public PhoneticEngine(NameType nameType, RuleType ruleType, boolean concat) {
		if (ruleType == RuleType.RULES)
			throw new IllegalArgumentException("ruleType must not be "
					+ RuleType.RULES);
		this.nameType = nameType;
		this.ruleType = ruleType;
		this.concat = concat;
		this.lang = Lang.instance(nameType);
	}

	private PhonemeBuilder applyFinalRules(PhonemeBuilder phonemeBuilder,
			List<Rule> finalRules) {
		if (finalRules == null)
			throw new NullPointerException("finalRules can not be null");
		if (finalRules.isEmpty())
			return phonemeBuilder;
		Set<Rule.Phoneme> phonemes = new TreeSet<Rule.Phoneme>(
				Rule.Phoneme.COMPARATOR);
		for (Rule.Phoneme phoneme : phonemeBuilder.getPhonemes()) {
			PhonemeBuilder subBuilder = PhonemeBuilder.empty(phoneme
					.getLanguages());
			CharSequence phonemeText = cacheSubSequence(phoneme
					.getPhonemeText());
			for (int i = 0; i < phonemeText.length();) {
				RulesApplication rulesApplication = new RulesApplication(
						finalRules, phonemeText, subBuilder, i).invoke();
				boolean found = rulesApplication.isFound();
				subBuilder = rulesApplication.getPhonemeBuilder();
				if (!found)
					subBuilder = subBuilder.append(phonemeText.subSequence(i,
							i + 1));
				i = rulesApplication.getI();
			}
			phonemes.addAll(subBuilder.getPhonemes());
		}
		return new PhonemeBuilder(phonemes);
	}

	public String encode(String input) {
		Languages.LanguageSet languageSet = this.lang.guessLanguages(input);
		return encode(input, languageSet);
	}

	public String encode(String input, final Languages.LanguageSet languageSet) {
		final List<Rule> rules = Rule.getInstance(this.nameType,
				RuleType.RULES, languageSet);
		final List<Rule> finalRules1 = Rule.getInstance(this.nameType,
				this.ruleType, "common");
		final List<Rule> finalRules2 = Rule.getInstance(this.nameType,
				this.ruleType, languageSet);
		input = input.toLowerCase(Locale.ENGLISH).replace('-', ' ').trim();
		if (this.nameType == NameType.GENERIC) {
			if (input.length() >= 2 && input.substring(0, 2).equals("d'")) { // check
																				// for
																				// d'
				String remainder = input.substring(2);
				String combined = "d" + remainder;
				return "(" + encode(remainder) + ")-(" + encode(combined) + ")";
			}
			for (String l : NAME_PREFIXES.get(this.nameType)) {
				if (input.startsWith(l + " ")) {
					String remainder = input.substring(l.length() + 1); // input
																		// without
																		// the
																		// prefix
					String combined = l + remainder; // input with prefix
														// without space
					return "(" + encode(remainder) + ")-(" + encode(combined)
							+ ")";
				}
			}
		}
		final List<String> words = Arrays.asList(input.split("\\s+"));
		final List<String> words2 = new ArrayList<String>();
		switch (this.nameType) {
		case SEPHARDIC:
			for (String aWord : words) {
				String[] parts = aWord.split("'");
				String lastPart = parts[parts.length - 1];
				words2.add(lastPart);
			}
			words2.removeAll(NAME_PREFIXES.get(this.nameType));
			break;
		case ASHKENAZI:
			words2.addAll(words);
			words2.removeAll(NAME_PREFIXES.get(this.nameType));
			break;
		case GENERIC:
			words2.addAll(words);
			break;
		default:
			throw new IllegalStateException("Unreachable case: "
					+ this.nameType);
		}
		if (this.concat)
			input = join(words2, " ");
		else if (words2.size() == 1)
			input = words.iterator().next();
		else {
			StringBuilder result = new StringBuilder();
			for (String word : words2)
				result.append("-").append(encode(word));
			return result.substring(1);
		}
		PhonemeBuilder phonemeBuilder = PhonemeBuilder.empty(languageSet);
		CharSequence inputCache = cacheSubSequence(input);
		for (int i = 0; i < inputCache.length();) {
			RulesApplication rulesApplication = new RulesApplication(rules,
					inputCache, phonemeBuilder, i).invoke();
			i = rulesApplication.getI();
			phonemeBuilder = rulesApplication.getPhonemeBuilder();
		}
		phonemeBuilder = applyFinalRules(phonemeBuilder, finalRules1);
		phonemeBuilder = applyFinalRules(phonemeBuilder, finalRules2);
		return phonemeBuilder.makeString();
	}

	public Lang getLang() {
		return this.lang;
	}

	public NameType getNameType() {
		return this.nameType;
	}

	public RuleType getRuleType() {
		return this.ruleType;
	}

	public boolean isConcat() {
		return this.concat;
	}
}