# Localization

The pickers adapt to the current `Locale` (`androidx.compose.ui.text.intl.Locale.current` by
default), including date order, month names, AM/PM text, CJK suffixes and numerals.

## Supported languages (30)

Arabic (العربية), Bengali (বাংলা), Chinese (中文), Czech (Čeština), Danish (Dansk), Dutch
(Nederlands), English, Finnish (Suomi), French (Français), German (Deutsch), Greek (Ελληνικά),
Hebrew (עברית), Hindi (हिन्दी), Indonesian (Bahasa Indonesia), Italian (Italiano), Japanese (日本語),
Korean (한국어), Norwegian (Norsk, `nb` and `no`), Persian (فارسی), Polish (Polski), Portuguese
(Português), Romanian (Română), Russian (Русский), Spanish (Español), Swedish (Svenska), Thai (ไทย),
Turkish (Türkçe), Ukrainian (Українська), Uzbek (Oʻzbekcha / Ўзбекча / اۉزبېکچه; `uz`, `uz-Cyrl`,
`uz-Arab`), Vietnamese (Tiếng Việt)

Found a translation error, or want another language? Please
[open an issue](https://github.com/darkokoa/datetime-wheel-picker/issues) or send a pull request.

## How a locale is matched

A locale is matched first by its `language` + `script` subtags, then by `language` alone, and
finally falls back to English.

| Requested | Resolved | Why |
|-----------|----------|-----|
| `uz-Arab` | `uz-Arab` | Direct language + script match |
| `uz-Latn` | `uz` | Script not bundled; base language used |
| `zh-Hant` | `zh` | Same |
| `sw` | English | Language not bundled |

**Limitation**: BCP 47 `-u-*` Unicode extension subtags (for example `-u-nu-*` for the numbering
system or `-u-ca-*` for the calendar) are ignored. Only `language` and `script` influence the
resolution.

## Date order and time format

- The `@Composable` `dateFormatter(locale, ...)` picks the date order from the locale: YMD for
  CJK, MDY for the United States, DMY elsewhere. Use the plain `dateFormatter(dateOrder = ...)`
  to choose it yourself.
- The `@Composable` `timeFormatter(locale)` uses the 12-hour format for English and the `US` /
  `GB` regions, and 24-hour otherwise.

## Numerals

Years, days, hours and minutes are rendered with the digits the resolved language defines, such
as the Eastern Arabic digits for Arabic, Persian and `uz-Arab`. Languages that do not define their
own digits keep `0`–`9`. Override `formatYear`, `formatDay`, `formatHour` or
`formatMinute` on the formatters to change that.

## CJK suffixes

Chinese and Japanese use 年/月/日 and Korean uses 년/월/일. Control them with `CjkSuffixConfig`;
see [Recipes](recipes.md#chinese--japanese--korean) and the [API reference](api.md#dateformatter).
