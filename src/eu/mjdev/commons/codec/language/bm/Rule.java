package eu.mjdev.commons.codec.language.bm;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rule {

	public static final class Phoneme implements PhonemeExpr {
		public static final Comparator<Phoneme> COMPARATOR = new Comparator<Phoneme>() {
			public int compare(Phoneme o1, Phoneme o2) {
				for (int i = 0; i < o1.phonemeText.length(); i++) {
					if (i >= o2.phonemeText.length())
						return +1;
					int c = o1.phonemeText.charAt(i) - o2.phonemeText.charAt(i);
					if (c != 0)
						return c;
				}
				if (o1.phonemeText.length() < o2.phonemeText.length())
					return -1;
				return 0;
			}
		};

		private final CharSequence phonemeText;
		private final Languages.LanguageSet languages;

		public Phoneme(CharSequence phonemeText, Languages.LanguageSet languages) {
			this.phonemeText = phonemeText;
			this.languages = languages;
		}

		public Phoneme append(CharSequence str) {
			return new Phoneme(this.phonemeText.toString() + str.toString(),
					this.languages);
		}

		public Languages.LanguageSet getLanguages() {
			return this.languages;
		}

		public Iterable<Phoneme> getPhonemes() {
			return Collections.singleton(this);
		}

		public CharSequence getPhonemeText() {
			return this.phonemeText;
		}

		public Phoneme join(Phoneme right) {
			return new Phoneme(this.phonemeText.toString()
					+ right.phonemeText.toString(),
					this.languages.restrictTo(right.languages));
		}
	}

	public interface PhonemeExpr {
		Iterable<Phoneme> getPhonemes();
	}

	public static final class PhonemeList implements PhonemeExpr {
		private final List<Phoneme> phonemes;

		public PhonemeList(List<Phoneme> phonemes) {
			this.phonemes = phonemes;
		}

		public List<Phoneme> getPhonemes() {
			return this.phonemes;
		}
	}

	public static interface RPattern {
		boolean isMatch(CharSequence input);
	}

	public static final RPattern ALL_STRINGS_RMATCHER = new RPattern() {
		public boolean isMatch(CharSequence input) {
			return true;
		}
	};

	public static final String ALL = "ALL";

	private static final String DOUBLE_QUOTE = "\"";

	private static final String HASH_INCLUDE = "#include";

	private static final Map<NameType, Map<RuleType, Map<String, List<Rule>>>> RULES = new EnumMap<NameType, Map<RuleType, Map<String, List<Rule>>>>(
			NameType.class);

	static {
		for (NameType s : NameType.values()) {
			Map<RuleType, Map<String, List<Rule>>> rts = new EnumMap<RuleType, Map<String, List<Rule>>>(
					RuleType.class);
			for (RuleType rt : RuleType.values()) {
				Map<String, List<Rule>> rs = new HashMap<String, List<Rule>>();
				Languages ls = Languages.getInstance(s);
				for (String l : ls.getLanguages()) {
					try {
						rs.put(l,
								parseRules(createScanner(s, rt, l),
										createResourceName(s, rt, l)));
					} catch (IllegalStateException e) {
						throw new IllegalStateException("Problem processing "
								+ createResourceName(s, rt, l), e);
					}
				}
				if (!rt.equals(RuleType.RULES))
					rs.put("common",
							parseRules(createScanner(s, rt, "common"),
									createResourceName(s, rt, "common")));
				rts.put(rt, Collections.unmodifiableMap(rs));
			}
			RULES.put(s, Collections.unmodifiableMap(rts));
		}
	}

	private static boolean contains(CharSequence chars, char input) {
		for (int i = 0; i < chars.length(); i++) {
			if (chars.charAt(i) == input)
				return true;
		}
		return false;
	}

	private static String createResourceName(NameType nameType, RuleType rt,
			String lang) {
		return String.format(
				"org/apache/commons/codec/language/bm/%s_%s_%s.txt",
				nameType.getName(), rt.getName(), lang);
	}

	private static Scanner createScanner(NameType nameType, RuleType rt,
			String lang) {
		String resName = createResourceName(nameType, rt, lang);
		InputStream rulesIS = Languages.class.getClassLoader()
				.getResourceAsStream(resName);
		if (rulesIS == null)
			throw new IllegalArgumentException("Unable to load resource: "
					+ resName);
		return new Scanner(rulesIS, ResourceConstants.ENCODING);
	}

	private static Scanner createScanner(String lang) {
		String resName = String.format(
				"org/apache/commons/codec/language/bm/%s.txt", lang);
		InputStream rulesIS = Languages.class.getClassLoader()
				.getResourceAsStream(resName);
		if (rulesIS == null)
			throw new IllegalArgumentException("Unable to load resource: "
					+ resName);
		return new Scanner(rulesIS, ResourceConstants.ENCODING);
	}

	private static boolean endsWith(CharSequence input, CharSequence suffix) {
		if (suffix.length() > input.length())
			return false;
		for (int i = input.length() - 1, j = suffix.length() - 1; j >= 0; i--, j--) {
			if (input.charAt(i) != suffix.charAt(j))
				return false;
		}
		return true;
	}

	public static List<Rule> getInstance(NameType nameType, RuleType rt,
			Languages.LanguageSet langs) {
		return langs.isSingleton() ? getInstance(nameType, rt, langs.getAny())
				: getInstance(nameType, rt, Languages.ANY);
	}

	public static List<Rule> getInstance(NameType nameType, RuleType rt,
			String lang) {
		List<Rule> rules = RULES.get(nameType).get(rt).get(lang);
		if (rules == null)
			throw new IllegalArgumentException(String.format(
					"No rules found for %s, %s, %s.", nameType.getName(),
					rt.getName(), lang));
		return rules;
	}

	private static Phoneme parsePhoneme(String ph) {
		int open = ph.indexOf("[");
		if (open >= 0) {
			if (!ph.endsWith("]"))
				throw new IllegalArgumentException(
						"Phoneme expression contains a '[' but does not end in ']'");
			String before = ph.substring(0, open);
			String in = ph.substring(open + 1, ph.length() - 1);
			Set<String> langs = new HashSet<String>(Arrays.asList(in
					.split("[+]")));
			return new Phoneme(before, Languages.LanguageSet.from(langs));
		} else {
			return new Phoneme(ph, Languages.ANY_LANGUAGE);
		}
	}

	private static PhonemeExpr parsePhonemeExpr(String ph) {
		if (ph.startsWith("(")) {
			if (!ph.endsWith(")"))
				throw new IllegalArgumentException(
						"Phoneme starts with '(' so must end with ')'");
			List<Phoneme> phs = new ArrayList<Phoneme>();
			String body = ph.substring(1, ph.length() - 1);
			for (String part : body.split("[|]"))
				phs.add(parsePhoneme(part));
			if (body.startsWith("|") || body.endsWith("|"))
				phs.add(new Phoneme("", Languages.ANY_LANGUAGE));
			return new PhonemeList(phs);
		} else {
			return parsePhoneme(ph);
		}
	}

	private static List<Rule> parseRules(final Scanner scanner,
			final String location) {
		List<Rule> lines = new ArrayList<Rule>();
		int currentLine = 0;
		boolean inMultilineComment = false;
		while (scanner.hasNextLine()) {
			currentLine++;
			String rawLine = scanner.nextLine();
			String line = rawLine;
			if (inMultilineComment) {
				if (line.endsWith(ResourceConstants.EXT_CMT_END))
					inMultilineComment = false;
			} else {
				if (line.startsWith(ResourceConstants.EXT_CMT_START))
					inMultilineComment = true;
				else {
					int cmtI = line.indexOf(ResourceConstants.CMT);
					if (cmtI >= 0)
						line = line.substring(0, cmtI);
					line = line.trim();
					if (line.length() == 0)
						continue;
					if (line.startsWith(HASH_INCLUDE)) {
						String incl = line.substring(HASH_INCLUDE.length())
								.trim();
						if (!incl.contains(" "))
							lines.addAll(parseRules(createScanner(incl),
									location + "->" + incl));
					} else {
						String[] parts = line.split("\\s+");
						if (parts.length == 4) {
							try {
								String pat = stripQuotes(parts[0]);
								String lCon = stripQuotes(parts[1]);
								String rCon = stripQuotes(parts[2]);
								PhonemeExpr ph = parsePhonemeExpr(stripQuotes(parts[3]));
								final int cLine = currentLine;
								Rule r = new Rule(pat, lCon, rCon, ph) {
									private final int myLine = cLine;
									private final String loc = location;

									@Override
									public String toString() {
										final StringBuilder sb = new StringBuilder();
										sb.append("Rule");
										sb.append("{line=").append(myLine);
										sb.append(", loc='").append(loc)
												.append('\'');
										sb.append('}');
										return sb.toString();
									}
								};
								lines.add(r);
							} catch (IllegalArgumentException e) {
								throw new IllegalStateException(
										"Problem parsing line " + currentLine,
										e);
							}
						}
					}
				}
			}
		}
		return lines;
	}

	private static RPattern pattern(final String regex) {
		boolean startsWith = regex.startsWith("^");
		boolean endsWith = regex.endsWith("$");
		final String content = regex.substring(startsWith ? 1 : 0,
				endsWith ? regex.length() - 1 : regex.length());
		boolean boxes = content.contains("[");
		if (!boxes) {
			if (startsWith && endsWith) {
				if (content.length() == 0) {
					return new RPattern() {
						public boolean isMatch(CharSequence input) {
							return input.length() == 0;
						}
					};
				} else {
					return new RPattern() {
						public boolean isMatch(CharSequence input) {
							return input.equals(content);
						}
					};
				}
			} else if ((startsWith || endsWith) && content.length() == 0)
				return ALL_STRINGS_RMATCHER;
			else if (startsWith)
				return new RPattern() {
					public boolean isMatch(CharSequence input) {
						return startsWith(input, content);
					}
				};
			else if (endsWith)
				return new RPattern() {
					public boolean isMatch(CharSequence input) {
						return endsWith(input, content);
					}
				};
		} else {
			boolean startsWithBox = content.startsWith("[");
			boolean endsWithBox = content.endsWith("]");

			if (startsWithBox && endsWithBox) {
				String boxContent = content.substring(1, content.length() - 1);
				if (!boxContent.contains("[")) {
					boolean negate = boxContent.startsWith("^");
					if (negate)
						boxContent = boxContent.substring(1);
					final String bContent = boxContent;
					final boolean shouldMatch = !negate;
					if (startsWith && endsWith)
						return new RPattern() {
							public boolean isMatch(CharSequence input) {
								return input.length() == 1
										&& (contains(bContent, input.charAt(0)) == shouldMatch);
							}
						};
					else if (startsWith)
						return new RPattern() {
							public boolean isMatch(CharSequence input) {
								return input.length() > 0
										&& (contains(bContent, input.charAt(0)) == shouldMatch);
							}
						};
					else if (endsWith)
						return new RPattern() {
							public boolean isMatch(CharSequence input) {
								return input.length() > 0
										&& (contains(
												bContent,
												input.charAt(input.length() - 1)) == shouldMatch);
							}
						};
				}
			}
		}
		return new RPattern() {
			Pattern pattern = Pattern.compile(regex);

			public boolean isMatch(CharSequence input) {
				Matcher matcher = pattern.matcher(input);
				return matcher.find();
			}
		};
	}

	private static boolean startsWith(CharSequence input, CharSequence prefix) {
		if (prefix.length() > input.length())
			return false;
		for (int i = 0; i < prefix.length(); i++) {
			if (input.charAt(i) != prefix.charAt(i))
				return false;
		}
		return true;
	}

	private static String stripQuotes(String str) {
		if (str.startsWith(DOUBLE_QUOTE))
			str = str.substring(1);
		if (str.endsWith(DOUBLE_QUOTE))
			str = str.substring(0, str.length() - 1);
		return str;
	}

	private final RPattern lContext;

	private final String pattern;

	private final PhonemeExpr phoneme;

	private final RPattern rContext;

	public Rule(String pattern, String lContext, String rContext,
			PhonemeExpr phoneme) {
		this.pattern = pattern;
		this.lContext = pattern(lContext + "$");
		this.rContext = pattern("^" + rContext);
		this.phoneme = phoneme;
	}

	public RPattern getLContext() {
		return this.lContext;
	}

	public String getPattern() {
		return this.pattern;
	}

	public PhonemeExpr getPhoneme() {
		return this.phoneme;
	}

	public RPattern getRContext() {
		return this.rContext;
	}

	public boolean patternAndContextMatches(CharSequence input, int i) {
		if (i < 0)
			throw new IndexOutOfBoundsException(
					"Can not match pattern at negative indexes");
		int patternLength = this.pattern.length();
		int ipl = i + patternLength;
		if (ipl > input.length())
			return false;
		boolean patternMatches = input.subSequence(i, ipl).equals(this.pattern);
		boolean rContextMatches = this.rContext.isMatch(input.subSequence(ipl,
				input.length()));
		boolean lContextMatches = this.lContext
				.isMatch(input.subSequence(0, i));
		return patternMatches && rContextMatches && lContextMatches;
	}
}