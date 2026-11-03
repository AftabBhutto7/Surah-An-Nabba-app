#!/usr/bin/env python3
import urllib.request
import json
import re

print("Building canonical datasets for Surah An-Naba (Surah 78) & Surah An-Nazi'at (Surah 79)...")

def clean_html(raw_html):
    if not raw_html:
        return ""
    clean = re.sub(r"<[^>]+>", "", raw_html)
    return clean.strip()

ROOT_DICT_NABA = {
    "عَمَّ": {"root": "عَنْ + مَا", "pos": "حرف جر + اسم استفهام", "role": "عن حرف جر وما اسم استفهام حذفت ألفها لدخول حرف الجر", "case": "مبني"},
    "يَتَسَآءَلُونَ": {"root": "س-أ-ل", "pos": "فعل مضارع", "role": "فعل مضارع مرفوع بثبوت النون والواو فاعل", "case": "مرفوع"},
    "عَنِ": {"root": "حرف جر", "pos": "حرف", "role": "حرف جر للمجاوزة", "case": "مبني"},
    "ٱلنَّبَإِ": {"root": "ن-ب-أ", "pos": "اسم", "role": "اسم مجرور بعن وعلامة جره الكسرة", "case": "مجرور"},
    "ٱلْعَظِيمِ": {"root": "ع-ظ-م", "pos": "اسم (نعت)", "role": "نعت للنبأ مجرور بالكسرة", "case": "مجرور"},
    "ٱلَّذِى": {"root": "اسم موصول", "pos": "اسم", "role": "نعت ثان في محل جر", "case": "مبني"},
    "هُمْ": {"root": "ضمير منفصل", "pos": "ضمير", "role": "مبتدأ مبني في محل رفع", "case": "مرفوع محلاً"},
    "فِيهِ": {"root": "فِي + هـ", "pos": "حرف جر + ضمير", "role": "جار ومجرور متعلق بمختلفون", "case": "مبني"},
    "مُخْتَلِفُونَ": {"root": "خ-ل-ف", "pos": "اسم فاعل جمع", "role": "خبر المبتدأ مرفوع بالواو", "case": "مرفوع"},
    "كَلَّا": {"root": "حرف ردع وزجر", "pos": "حرف", "role": "حرف ردع وزجر وتنبيه", "case": "مبني"},
    "سَيَعْلَمُونَ": {"root": "ع-ل-م", "pos": "سين الاستقبال + فعل مضارع", "role": "فعل مضارع مرفوع بثبوت النون والواو فاعل", "case": "مرفوع"},
    "ثُمَّ": {"root": "حرف عطف", "pos": "حرف", "role": "حرف عطف للترتيب مع التراخي", "case": "مبني"},
    "أَلَمْ": {"root": "همزة استفهام + لم", "pos": "حرف جزم", "role": "حرف نفي وجزم وقلب للتقرير", "case": "مبني"},
    "نَجْعَلِ": {"root": "ج-ع-ل", "pos": "فعل مضارع مجزوم بلم", "role": "فعل مضارع مجزوم بالسكون وحرك بالكسر لالتقاء الساكنين", "case": "مجزوم"},
    "ٱلْأَرْضَ": {"root": "أ-ر-ض", "pos": "اسم", "role": "مفعول به أول منصوب بالفتحة", "case": "منصوب"},
    "مِهَٰدًا": {"root": "م-ه-د", "pos": "اسم (فراشاً ممهداً)", "role": "مفعول به ثان منصوب بالفتحة", "case": "منصوب"},
    "وَٱلْجِبَالَ": {"root": "ج-ب-ل", "pos": "اسم جمع", "role": "معطوف على الأرض منصوب بالفتحة", "case": "منصوب"},
    "أَوْتَادًا": {"root": "و-ت-د", "pos": "اسم جمع (مثبتات كالأوتاد)", "role": "مفعول به منصوب بالفتحة", "case": "منصوب"},
    "وَخَلَقْنَٰكُمْ": {"root": "خ-ل-ق", "pos": "فعل ماض + نا + كم", "role": "فعل ماض مبني على السكون ونا فاعل وكم مفعول به", "case": "مبني"},
    "أَزْوَٰجًا": {"root": "ز-و-ج", "pos": "اسم جمع (ذكوراً وإناثاً)", "role": "حال منصوبة أو مفعول ثان", "case": "منصوب"},
    "وَجَعَلْنَا": {"root": "ج-ع-ل", "pos": "فعل ماض + نا", "role": "فعل ماض مبني على السكون ونا فاعل", "case": "مبني"},
    "نَوْمَكُمْ": {"root": "ن-و-م", "pos": "اسم + كم", "role": "مفعول به أول منصوب بالفتحة", "case": "منصوب"},
    "سُبَاتًا": {"root": "س-ب-ت", "pos": "اسم (راحة وقطعاً للحركة)", "role": "مفعول به ثان منصوب بالفتحة", "case": "منصوب"},
    "ٱلَّيْلَ": {"root": "ل-ي-ل", "pos": "اسم", "role": "مفعول به أول منصوب بالفتحة", "case": "منصوب"},
    "لِبَاسًا": {"root": "ل-ب-س", "pos": "اسم (ساتراً بسواده كاللباس)", "role": "مفعول به ثان منصوب بالفتحة", "case": "منصوب"},
    "ٱلنَّهَارَ": {"root": "ن-ه-ر", "pos": "اسم", "role": "مفعول به أول منصوب بالفتحة", "case": "منصوب"},
    "مَعَاشًا": {"root": "ع-ي-ش", "pos": "اسم زمان/مكان (وقتاً لكسب المعيشة)", "role": "مفعول به ثان منصوب بالفتحة", "case": "منصوب"},
    "وَبَنَيْنَا": {"root": "ب-ن-ي", "pos": "فعل ماض + نا", "role": "فعل ماض مبني على السكون ونا فاعل", "case": "مبني"},
    "فَوْقَكُمْ": {"root": "ف-و-ق", "pos": "ظرف مكان + كم", "role": "ظرف مكان منصوب متعلق ببنينا", "case": "منصوب"},
    "سَبْعًا": {"root": "س-ب-ع", "pos": "اسم عدد (سبع سموات)", "role": "مفعول به منصوب بالفتحة", "case": "منصوب"},
    "شِدَادًا": {"root": "ش-د-د", "pos": "اسم (نعت لسبعاً، قوية محكمة)", "role": "نعت لسبعاً منصوب بالفتحة", "case": "منصوب"},
    "سِرَاجًا": {"root": "س-ر-ج", "pos": "اسم (مصباحاً وهو الشمس)", "role": "مفعول به أول منصوب بالفتحة", "case": "منصوب"},
    "وَهَّاجًا": {"root": "و-ه-ج", "pos": "اسم (صفة مبالغة، متلألئاً وقاداً بالحرارة والضوء)", "role": "نعت لسراجاً منصوب بالفتحة", "case": "منصوب"},
    "وَأَنزَلْنَا": {"root": "ن-ز-ل", "pos": "فعل ماض + نا", "role": "فعل ماض مبني على السكون ونا فاعل", "case": "مبني"},
    "ٱلْمُعْصِرَٰتِ": {"root": "ع-ص-ر", "pos": "اسم جمع (السحب الممطرة إذا حان عصرها)", "role": "اسم مجرور بمن", "case": "مجرور"},
    "مَآءً": {"root": "م-و-ه", "pos": "اسم", "role": "مفعول به منصوب بالفتحة", "case": "منصوب"},
    "ثَجَّاجًا": {"root": "ث-ج-ج", "pos": "اسم (نعت، منصباً متدفقاً بغزارة)", "role": "نعت لماءً منصوب بالفتحة", "case": "منصوب"},
    "لِّنُخْرِجَ": {"root": "خ-ر-ج", "pos": "لام التعليل + فعل مضارع", "role": "فعل مضارع منصوب بأن مضمرة بعد لام التعليل", "case": "منصوب"},
    "بِهِۦ": {"root": "باء + هـ", "pos": "حرف جر + ضمير", "role": "جار ومجرور متعلق بنخرج", "case": "مبني"},
    "حَبًّۭا": {"root": "ح-ب-ب", "pos": "اسم", "role": "مفعول به لنخرج منصوب بالفتحة", "case": "منصوب"},
    "نَّبَاتًا": {"root": "ن-ب-ت", "pos": "اسم (مصدر أنبت)", "role": "معطوف على حباً منصوب بالفتحة", "case": "منصوب"},
    "جَنَّٰتٍ": {"root": "ج-ن-ن", "pos": "اسم جمع مؤنث سالم (بساتين)", "role": "معطوف منصوب وعلامة نصبه الكسرة نيابة عن الفتحة", "case": "منصوب"},
    "أَلْفَافًا": {"root": "ل-ف-ف", "pos": "اسم جمع (ملتفة الأشجار والأغصان)", "role": "نعت لجنات منصوب بالفتحة", "case": "منصوب"},
    "إِنَّ": {"root": "حرف توكيد ونصب", "pos": "حرف", "role": "حرف مشبه بالفعل ينصب الاسم ويرفع الخبر", "case": "مبني"},
    "يَوْمَ": {"root": "ي-و-م", "pos": "اسم", "role": "اسم إن منصوب وعلامة نصبه الفتحة", "case": "منصوب"},
    "ٱلْفَصْلِ": {"root": "ف-ص-ل", "pos": "اسم (يوم القضاء بين الخلائق)", "role": "مضاف إليه مجرور بالكسرة", "case": "مجرور"},
    "كَانَ": {"root": "ك-و-ن", "pos": "فعل ماض ناقص", "role": "فعل ماض ناقص يرفع الاسم وينصب الخبر", "case": "مبني"},
    "مِيقَٰتًا": {"root": "و-ق-ت", "pos": "اسم زمان (وقتاً محدداً لا يتقدم ولا يتأخر)", "role": "خبر كان منصوب بالفتحة", "case": "منصوب"},
    "يُنفَخُ": {"root": "ن-ف-خ", "pos": "فعل مضارع مبني للمجهول", "role": "فعل مضارع مرفوع بالضمة ونائب الفاعل ضمير مستتر", "case": "مرفوع"},
    "ٱلصُّورِ": {"root": "ص-و-ر", "pos": "اسم (القرن)", "role": "اسم مجرور بفي وعلامة جره الكسرة", "case": "مجرور"},
    "فَتَأْتُونَ": {"root": "أ-ت-ي", "pos": "فاء العطف + فعل مضارع", "role": "فعل مضارع مرفوع بثبوت النون والواو فاعل", "case": "مرفوع"},
    "أَفْوَاجًا": {"root": "ف-و-ج", "pos": "اسم جمع (زمراً وجماعات متتابعة)", "role": "حال منصوبة وعلامة نصبها الفتحة", "case": "منصوب"},
    "وَفُتِحَتِ": {"root": "ف-ت-ح", "pos": "فعل ماض مبني للمجهول", "role": "فعل ماض مبني على الفتح وتاء التأنيث", "case": "مبني"},
    "ٱلسَّمَآءُ": {"root": "س-م-و", "pos": "اسم", "role": "نائب فاعل مرفوع وعلامة رفعه الضمة", "case": "مرفوع"},
    "أَبْوَٰبًا": {"root": "ب-و-ب", "pos": "اسم جمع", "role": "خبر كان محذوفة أو حال منصوبة", "case": "منصوب"},
    "وَسُيِّرَتِ": {"root": "س-ي-ر", "pos": "فعل ماض مبني للمجهول", "role": "فعل ماض مبني على الفتح (فتتت ونزعت من أصولها)", "case": "مبني"},
    "ٱلْجِبَالُ": {"root": "ج-ب-ل", "pos": "اسم جمع", "role": "نائب فاعل مرفوع بالضمة", "case": "مرفوع"},
    "سَرَابًا": {"root": "س-ر-ب", "pos": "اسم (لا حقيقة لها كالسراب)", "role": "حال أو خبر كان منصوب بالفتحة", "case": "منصوب"},
    "جَهَنَّمَ": {"root": "ج-ه-ن-م", "pos": "اسم علم للنار (ممنوع من الصرف)", "role": "اسم إن منصوب بالفتحة بدون تنوين", "case": "منصوب"},
    "مِرْصَادًا": {"root": "ر-ص-د", "pos": "اسم مكان/مبالغة (تترصد الكفار وتنتظرهم)", "role": "خبر كان منصوب بالفتحة", "case": "منصوب"},
    "لِّلطَّٰغِينَ": {"root": "ط-غ-ي", "pos": "لام الجر + اسم فاعل جمع", "role": "اسم مجرور باللام وعلامة جره الياء", "case": "مجرور"},
    "مَـَٔابًا": {"root": "أ-و-ب", "pos": "اسم مكان (مرجعاً ومستقراً ومأوى)", "role": "بدل من مرصاداً أو خبر ثان منصوب بالفتحة", "case": "منصوب"},
    "لَّٰبِثِينَ": {"root": "ل-ب-ث", "pos": "اسم فاعل جمع (ماكثين مقيمين)", "role": "حال منصوبة وعلامة نصبها الياء", "case": "منصوب"},
    "أَحْقَابًا": {"root": "ح-ق-ب", "pos": "اسم جمع (دهوراً متتابعة لا تنقطع، الحقب 80 سنة)", "role": "ظرف زمان منصوب بالفتحة", "case": "منصوب"},
    "لَّا يَذُوقُونَ": {"root": "ذ-و-ق", "pos": "لا النافية + فعل مضارع", "role": "فعل مضارع مرفوع بثبوت النون والواو فاعل", "case": "مرفوع"},
    "بَرْدًا": {"root": "ب-ر-د", "pos": "اسم (هواء بارداً يستريحون به أو نوماً)", "role": "مفعول به منصوب بالفتحة", "case": "منصوب"},
    "شَرَابًا": {"root": "ش-ر-ب", "pos": "اسم (ماء عذباً سائغاً)", "role": "معطوف على برداً منصوب بالفتحة", "case": "منصوب"},
    "حَمِيمًا": {"root": "ح-م-م", "pos": "اسم (ماء بالغ الغليان يقطع الأمعاء)", "role": "مستثنى منصوب أو بدل", "case": "منصوب"},
    "غَسَّاقًا": {"root": "غ-س-ق", "pos": "اسم (صديد أهل النار ودموعهم المنتنة)", "role": "معطوف على حميماً منصوب بالفتحة", "case": "منصوب"},
    "جَزَآءً": {"root": "ج-ز-ي", "pos": "اسم (مصدر)", "role": "مفعول مطلق أو مفعول لأجله منصوب بالفتحة", "case": "منصوب"},
    "وِفَاقًا": {"root": "و-ف-ق", "pos": "اسم (موافقاً ومطابقاً لأعمالهم السيئة بعدل)", "role": "نعت لجزاءً منصوب بالفتحة", "case": "منصوب"},
    "يَرْجُونَ": {"root": "ر-ج-و", "pos": "فعل مضارع جمع (يخافون أو يترقبون)", "role": "فعل مضارع مرفوع بثبوت النون والواو فاعل", "case": "مرفوع"},
    "حِسَابًا": {"root": "ح-س-ب", "pos": "اسم", "role": "مفعول به يرجون منصوب بالفتحة", "case": "منصوب"},
    "وَكَذَّبُوا۟": {"root": "ك-ذ-ب", "pos": "فعل ماض + واو الجماعة", "role": "فعل ماض مبني على الضم والواو فاعل", "case": "مبني"},
    "بِـَٔايَٰتِنَا": {"root": "أ-ي-ي", "pos": "باء الجر + اسم جمع + نا", "role": "جار ومجرور ومضاف إليه", "case": "مجرور"},
    "كِذَّابًا": {"root": "ك-ذ-ب", "pos": "اسم (مصدر كذب كذاباً، تكذيباً شديداً)", "role": "مفعول مطلق مؤكد منصوب بالفتحة", "case": "منصوب"},
    "وَكُلَّ": {"root": "ك-ل-ل", "pos": "اسم", "role": "مفعول به لفعل محذوف يفسره أحصيناه منصوب بالفتحة", "case": "منصوب"},
    "شَىْءٍ": {"root": "ش-ي-أ", "pos": "اسم", "role": "مضاف إليه مجرور بالكسرة", "case": "مجرور"},
    "أَحْصَيْنَٰهُ": {"root": "ح-ص-ي", "pos": "فعل ماض + نا + هـ", "role": "فعل ماض مبني على السكون والهاء مفعول به", "case": "مبني"},
    "كِتَٰبًا": {"root": "ك-ت-ب", "pos": "اسم (مكتوباً في اللوح المحفوظ)", "role": "حال منصوبة أو مفعول مطلق", "case": "منصوب"},
    "فَذُوقُوا۟": {"root": "ذ-و-ق", "pos": "فاء الفصيحة + فعل أمر", "role": "فعل أمر مبني على حذف النون والواو فاعل", "case": "مبني"},
    "فَلَن": {"root": "فاء + لن", "pos": "حرف نفي ونصب واستقبال", "role": "حرف ينصب الفعل المضارع مؤبداً", "case": "مبني"},
    "نَّزِيدَكُمْ": {"root": "ز-ي-د", "pos": "فعل مضارع منصوب بلن + كم", "role": "فعل مضارع منصوب بالفتحة وكم مفعول به", "case": "منصوب"},
    "عَذَابًا": {"root": "ع-ذ-ب", "pos": "اسم", "role": "مستثنى منصوب أو مفعول ثان منصوب", "case": "منصوب"},
    "لِّلْمُتَّقِينَ": {"root": "و-ق-ي", "pos": "لام الجر + اسم فاعل جمع", "role": "شبه جملة خبر إن مقدم في محل رفع", "case": "مجرور"},
    "مَفَازًا": {"root": "ف-و-ز", "pos": "اسم مكان/مصدر ميمي (فوزاً وظفراً بالجنة ونجاة من النار)", "role": "اسم إن مؤخر منصوب بالفتحة", "case": "منصوب"},
    "حَدَآئِقَ": {"root": "ح-د-ق", "pos": "اسم جمع (بساتين محوطة بالجدران)", "role": "بدل من مفازاً منصوب بالفتحة بدون تنوين لمنعه من الصرف", "case": "منصوب"},
    "أَعْنَٰبًا": {"root": "ع-ن-ب", "pos": "اسم جمع", "role": "معطوف على حدائق منصوب بالفتحة", "case": "منصوب"},
    "وَكَوَاعِبَ": {"root": "ك-ع-ب", "pos": "اسم جمع (حوريات نواهد استدارت صدورهن من الصبا)", "role": "معطوف منصوب بالفتحة", "case": "منصوب"},
    "أَتْرَابًا": {"root": "ت-ر-ب", "pos": "اسم جمع (في سن واحدة متقاربة)", "role": "نعت لكواعب منصوب بالفتحة", "case": "منصوب"},
    "كَأْسًا": {"root": "ك-أ-س", "pos": "اسم (قدحاً من خمر الجنة)", "role": "معطوف منصوب بالفتحة", "case": "منصوب"},
    "دِهَاقًا": {"root": "د-ه-ق", "pos": "اسم (نعت، ملأى متتابعة صافية)", "role": "نعت لكأساً منصوب بالفتحة", "case": "منصوب"},
    "لَّا يَسْمَعُونَ": {"root": "س-م-ع", "pos": "لا النافية + فعل مضارع", "role": "فعل مضارع مرفوع بثبوت النون والواو فاعل", "case": "مرفوع"},
    "لَغْوًا": {"root": "ل-غ-و", "pos": "اسم (كلاماً ساقطاً باطلاً)", "role": "مفعول به منصوب بالفتحة", "case": "منصوب"},
    "كِذَّٰبًا": {"root": "ك-ذ-ب", "pos": "اسم", "role": "معطوف على لغواً منصوب بالفتحة", "case": "منصوب"},
    "عَطَآءً": {"root": "ع-ط-و", "pos": "اسم (مصدر)", "role": "بدل من جزاءً أو مفعول مطلق منصوب بالفتحة", "case": "منصوب"},
    "حِسَابًا": {"root": "ح-س-ب", "pos": "اسم (كافياً شافياً بقدر أعمالهم وأضعافها)", "role": "نعت لعطاءً منصوب بالفتحة", "case": "منصوب"},
    "رَّبِّ": {"root": "ر-ب-ب", "pos": "اسم", "role": "بدل من ربك مجرور بالكسرة أو خبر لمبتدأ محذوف", "case": "مجرور"},
    "ٱلسَّمَٰوَٰتِ": {"root": "س-م-و", "pos": "اسم جمع مؤنث سالم", "role": "مضاف إليه مجرور بالكسرة", "case": "مجرور"},
    "ٱلرَّحْمَٰنِ": {"root": "ر-ح-م", "pos": "اسم", "role": "بدل أو نعت مجرور بالكسرة", "case": "مجرور"},
    "خِطَابًا": {"root": "خ-ط-ب", "pos": "اسم (مصدر، كلاماً ومراجعة هيبة منه)", "role": "مفعول به يملكون منصوب بالفتحة", "case": "منصوب"},
    "يَقُومُ": {"root": "ق-و-م", "pos": "فعل مضارع", "role": "فعل مضارع مرفوع بالضمة", "case": "مرفوع"},
    "ٱلرُّوحُ": {"root": "ر-و-ح", "pos": "اسم (جبريل عليه السلام أو خلق عظيم)", "role": "فاعل يقوم مرفوع وعلامة رفعه الضمة", "case": "مرفوع"},
    "وَٱلْمَلَٰٓئِكَةُ": {"root": "ل-أ-ك", "pos": "اسم جمع", "role": "معطوف على الروح مرفوع بالضمة", "case": "مرفوع"},
    "صَفًّۭا": {"root": "ص-ف-ف", "pos": "اسم (مصدر)", "role": "حال منصوبة وعلامة نصبها الفتحة (مصطفين خاشعين)", "case": "منصوب"},
    "لَّا يَتَكَلَّمُونَ": {"root": "ك-ل-م", "pos": "لا النافية + فعل مضارع", "role": "فعل مضارع مرفوع بثبوت النون والواو فاعل", "case": "مرفوع"},
    "أَذِنَ": {"root": "أ-ذ-ن", "pos": "فعل ماض", "role": "فعل ماض مبني على الفتح", "case": "مبني"},
    "صَوَابًا": {"root": "ص-و-ب", "pos": "اسم (حقاً وتوحيداً وشفاعة بالحق)", "role": "مفعول به قال منصوب بالفتحة", "case": "منصوب"},
    "ٱلْيَوْمُ": {"root": "ي-و-م", "pos": "اسم", "role": "مبتدأ مؤخر أو خبر مرفوع بالضمة", "case": "مرفوع"},
    "ٱلْحَقُّ": {"root": "ح-ق-ق", "pos": "اسم (نعت، الواقع الكائن لا محالة)", "role": "نعت لليوم مرفوع بالضمة", "case": "مرفوع"},
    "شَآءَ": {"root": "ش-ي-أ", "pos": "فعل ماض فعل الشرط", "role": "فعل ماض مبني على الفتح في محل جزم والفاعل مستتر", "case": "مبني"},
    "ٱتَّخَذَ": {"root": "أ-خ-ذ", "pos": "فعل ماض جواب الشرط", "role": "فعل ماض مبني على الفتح في محل جزم (سلك إلى ربه سبيلاً بالطاعة)", "case": "مبني"},
    "مَـَٔابًا": {"root": "أ-و-ب", "pos": "اسم مكان (مرجعاً ومصيراً حسناً بالجنة)", "role": "مفعول به ثان منصوب بالفتحة", "case": "منصوب"},
    "أَنذَرْنَٰكُمْ": {"root": "ن-ذ-ر", "pos": "فعل ماض + نا + كم", "role": "فعل ماض مبني على السكون ونا فاعل وكم مفعول أول", "case": "مبني"},
    "عَذَابًا": {"root": "ع-ذ-ب", "pos": "اسم", "role": "مفعول به ثان منصوب بالفتحة", "case": "منصوب"},
    "قَرِيبًا": {"root": "ق-ر-ب", "pos": "اسم (نعت، لأن كل آت قريب)", "role": "نعت لعذاباً منصوب بالفتحة", "case": "منصوب"},
    "يَنظُرُ": {"root": "ن-ظ-ر", "pos": "فعل مضارع (يرى ويعاين)", "role": "فعل مضارع مرفوع بالضمة", "case": "مرفوع"},
    "ٱلْمَرْءُ": {"root": "م-ر-ء", "pos": "اسم (الإنسان)", "role": "فاعل ينظر مرفوع وعلامة رفعه الضمة", "case": "مرفوع"},
    "قَدَّمَتْ": {"root": "ق-د-م", "pos": "فعل ماض + تاء التأنيث", "role": "فعل ماض مبني على الفتح", "case": "مبني"},
    "يَدَاهُ": {"root": "ي-د-ي", "pos": "اسم مثنى + هـ (ما كسبه وعمله في دنياه)", "role": "فاعل قدمت مرفوع بالألف لأنه مثنى وحذفت النون للإضافة", "case": "مرفوع"},
    "يَقُولُ": {"root": "ق-و-ل", "pos": "فعل مضارع", "role": "فعل مضارع مرفوع بالضمة", "case": "مرفوع"},
    "ٱلْكَافِرُ": {"root": "ك-ف-ر", "pos": "اسم فاعل", "role": "فاعل يقول مرفوع بالضمة", "case": "مرفوع"},
    "يَٰلَيْتَنِى": {"root": "ل-ي-ت", "pos": "ياء النداء أو التنبيه + ليت (حرف تمني) + النون والياء", "role": "ليت تنصب الاسم وترفع الخبر والياء اسمها", "case": "مبني"},
    "كُنتُ": {"root": "ك-و-ن", "pos": "فعل ماض ناقص + تاء الفاعل", "role": "فعل ماض ناقص والتاء اسمه", "case": "مبني"},
    "تُرَٰبًۢا": {"root": "ت-ر-ب", "pos": "اسم (كالحيوانات التي تصير تراباً بعد القضاء)", "role": "خبر كان منصوب بالفتحة", "case": "منصوب"}
}

def identify_tajweed(arabic_text):
    tokens = []
    if "نّ" in arabic_text or "مّ" in arabic_text:
        match = re.search(r"[نم]ّ", arabic_text)
        if match:
            tokens.append({"rule": "Ghunnah", "arabic": match.group(0), "explanation": "Nasal sound held for 2 counts on Noon/Meem Mushaddadah."})
    qalqalah_match = re.search(r"[قطبجد]ْ|[قطبجد]$", arabic_text)
    if qalqalah_match:
        tokens.append({"rule": "Qalqalah", "arabic": qalqalah_match.group(0), "explanation": "Crisp bouncing echo on Qaf, Taa, Baa, Jeem, or Daal when sakin."})
    if "ٓ" in arabic_text or "ٰ" in arabic_text:
        tokens.append({"rule": "Madd", "arabic": "ـٓـ", "explanation": "Prolongation of the vowel sound (4 to 6 counts)."})
    if re.search(r"ن[تثجدذزسشصضطظفقك]", arabic_text):
        tokens.append({"rule": "Ikhfa", "arabic": "إخفاء", "explanation": "Concealing Noon Sakinah with nasalization before an Ikhfa letter."})
    if re.search(r"ن[يرملون]", arabic_text):
        tokens.append({"rule": "Idgham", "arabic": "إدغام", "explanation": "Merging Noon Sakinah into the following letter."})
    if not tokens:
        tokens.append({"rule": "Tarqeeq / Tafkheem", "arabic": "ترقيق وتفخيم", "explanation": "Correct lightness or heaviness of pronunciation."})
    return tokens

# Themes for Surah An-Naba
def get_naba_theme(v_num):
    if 1 <= v_num <= 5:
        return "The Great News (Resurrection Dispute)"
    elif 6 <= v_num <= 16:
        return "Signs in Creation: Earth, Mountains, Sleep, Night & Sun"
    elif 17 <= v_num <= 30:
        return "The Day of Decision & The Terrors of Hellfire"
    elif 31 <= v_num <= 36:
        return "The Triumph & Delight of the Righteous in Paradise"
    else:
        return "The Day the Spirit and Angels Stand in Ranks & The Final Regret"

# Fetch Surah 78
url_78 = "https://api.quran.com/api/v4/verses/by_chapter/78?language=en&words=true&translations=20,97&fields=text_uthmani&per_page=40"
req_78 = urllib.request.Request(url_78, headers={"User-Agent": "Mozilla/5.0 (QuranPara30App/1.0)"})
with urllib.request.urlopen(req_78, timeout=20) as resp:
    data_78 = json.loads(resp.read().decode("utf-8"))

verses_78_raw = data_78.get("verses", [])
print(f"Surah 78 retrieved: {len(verses_78_raw)} verses.")

ayahs_78 = []
for v in verses_78_raw:
    v_num = v["verse_number"]
    text_uthmani = v.get("text_uthmani", "").strip()
    translations = v.get("translations", [])

    trans_en = ""
    trans_ur = ""
    for t in translations:
        res_id = t.get("resource_id")
        t_text = clean_html(t.get("text", ""))
        if res_id == 20:
            trans_en = t_text
        elif res_id == 97:
            trans_ur = t_text

    raw_words = v.get("words", [])
    words_list = []
    transliteration_parts = []

    for w in raw_words:
        if w.get("char_type_name") != "word":
            continue
        pos = w.get("position", 1)
        w_ar = w.get("text_uthmani") or w.get("text", "")
        w_en = clean_html(w.get("translation", {}).get("text", ""))
        w_tr = clean_html(w.get("transliteration", {}).get("text", ""))
        if w_tr:
            transliteration_parts.append(w_tr)

        clean_w = re.sub(r"[\u064B-\u065F\u0670\u06D6-\u06ED]", "", w_ar)
        dict_match = ROOT_DICT_NABA.get(w_ar) or ROOT_DICT_NABA.get(clean_w) or {}

        words_list.append({
            "position": pos,
            "arabic": w_ar,
            "translationEn": w_en,
            "translationUr": "",
            "transliteration": w_tr,
            "root": dict_match.get("root", "—"),
            "partOfSpeech": dict_match.get("pos", "كلمة قرآنية"),
            "grammarRole": dict_match.get("role", "معربة بحسب السياق"),
            "caseState": dict_match.get("case", "مبني / معرب"),
            "explanation": f"Surah An-Naba, Ayah {v_num}, Word {pos}"
        })

    padded_surah = "078"
    padded_ayah = str(v_num).zfill(3)
    audio_code = f"{padded_surah}{padded_ayah}"

    ayahs_78.append({
        "number": v_num,
        "surahNumber": 78,
        "arabicUthmani": text_uthmani,
        "translationEn": trans_en,
        "translationUr": trans_ur,
        "transliteration": " ".join(transliteration_parts),
        "theme": get_naba_theme(v_num),
        "audioUrlAlafasy": f"https://everyayah.com/data/Alafasy_128kbps/{audio_code}.mp3",
        "audioUrlAbdulBasit": f"https://everyayah.com/data/Abdul_Basit_Murattal_192kbps/{audio_code}.mp3",
        "audioUrlGhamadi": f"https://everyayah.com/data/Ghamadi_40kbps/{audio_code}.mp3",
        "words": words_list,
        "tajweedTokens": identify_tajweed(text_uthmani)
    })

# Save Surah An-Naba JSON
output_78 = "app/src/main/assets/surah_naba.json"
with open(output_78, "w", encoding="utf-8") as f:
    json.dump({
        "surahNumber": 78,
        "surahNameArabic": "النبأ",
        "surahNameEnglish": "An-Naba",
        "surahNameMeaning": "The Great News (Opening of Juz 30 / عم)",
        "totalAyahs": len(ayahs_78),
        "revelationType": "Meccan",
        "rukuCount": 2,
        "ayahs": ayahs_78
    }, f, ensure_ascii=False, indent=2)

print(f"Saved {output_78} with {len(ayahs_78)} ayahs.")

# Also generate Juz 30 authentic Hadiths
hadiths_juz30 = [
    {
        "id": 1,
        "titleEn": "Virtue of Reciting Juz 'Amma (Para 30)",
        "titleUr": "تیسویں پارے (عم پارہ) کی فضیلت",
        "arabicText": "كَانَ رَسُولُ اللَّهِ صلى الله عليه وسلم يُوتِرُ بِـ سَبِّحِ اسْمَ رَبِّكَ الأَعْلَى وَ قُلْ يَا أَيُّهَا الْكَافِرُونَ وَ قُلْ هُوَ اللَّهُ أَحَدٌ",
        "translationEn": "The Messenger of Allah (peace be upon him) used to recite in the Witr prayer: Surah Al-A'la, Surah Al-Kafirun, and Surah Al-Ikhlas from Juz 'Amma.",
        "translationUr": "رسول اللہ صلی اللہ علیہ وسلم نمازِ وتر میں تیسویں پارے کی سورتیں: سبح اسم ربک الاعلی، قل یا ایھا الکافرون اور قل ھو اللہ احد پڑھا کرتے تھے۔",
        "sourceCollection": "Sunan an-Nasa'i & Sunan Abi Dawud",
        "referenceId": "Sunan an-Nasa'i 1730, Abu Dawud 1423",
        "scholarlyGrading": "Sahih (Authentic / صحیح)",
        "scholarlyCommentary": "Narrated by Ubayy ibn Ka'b. Authentic narration highlighting how the Prophet (pbuh) regularly concluded his night worship with the profound chapters of Juz 30."
    },
    {
        "id": 2,
        "titleEn": "Surah An-Naba and the Warning of Judgment",
        "titleUr": "سورہ النبأ اور روزِ قیامت کی تذکیر",
        "arabicText": "شَيَّبَتْنِي هُودٌ وَالْوَاقِعَةُ وَالْمُرْسَلاَتُ وَعَمَّ يَتَسَاءَلُونَ وَإِذَا الشَّمْسُ كُوِّرَتْ",
        "translationEn": "Abu Bakr (ra) said: 'O Messenger of Allah, you have grown grey!' He replied: 'Surah Hud, Al-Waqi'ah, Al-Mursalat, 'Amma Yatasa'aloon (Surah An-Naba), and Idhash-Shamsu Kuwwirat have made me grey.'",
        "translationUr": "حضرت ابوبکر صدیق رضی اللہ عنہ نے عرض کیا: یا رسول اللہ! آپ بوڑھے ہو گئے۔ آپ صلی اللہ علیہ وسلم نے فرمایا: مجھے سورہ ہود، الواقعہ، المرسلات، عم يتساءلون (سورہ النبأ) اور اذا الشمس کورت نے بوڑھا کر دیا ہے۔",
        "sourceCollection": "Jami` at-Tirmidhi",
        "referenceId": "Hadith 3297 (Book 47, Hadith 349)",
        "scholarlyGrading": "Sahih / Hasan according to Imam at-Tirmidhi and Al-Albani",
        "scholarlyCommentary": "Narrated by Ibn Abbas (ra). Explains the deep psychological and spiritual weight of Surah An-Naba's vivid depictions of the Resurrection and the Day of Reckoning."
    },
    {
        "id": 3,
        "titleEn": "The Multiplication of Quranic Rewards",
        "titleUr": "تلاوتِ قرآن پر ہر حرف کا دس گنا اجر",
        "arabicText": "مَنْ قَرَأَ حَرْفًا مِنْ كِتَابِ اللَّهِ فَلَهُ بِهِ حَسَنَةٌ، وَالحَسَنَةُ بِعَشْرِ أَمْثَالِهَا",
        "translationEn": "Whoever recites a single letter from the Book of Allah receives one good deed, and one good deed is multiplied tenfold.",
        "translationUr": "جس نے اللہ کی کتاب کا ایک حرف پڑھا اس کے لیے ایک نیکی ہے اور ایک نیکی کا ثواب دس گنا ہے۔",
        "sourceCollection": "Jami` at-Tirmidhi",
        "referenceId": "Hadith 2910",
        "scholarlyGrading": "Sahih (Authentic / صحیح)",
        "scholarlyCommentary": "Narrated by Abdullah ibn Mas'ud. Reciting Surah An-Naba (comprising over 700 Arabic letters) yields thousands of divine rewards."
    }
]

with open("app/src/main/assets/hadith_naba.json", "w", encoding="utf-8") as f:
    json.dump(hadiths_juz30, f, ensure_ascii=False, indent=2)

print("Generated app/src/main/assets/hadith_naba.json.")

# Grammar lessons focused on Surah An-Naba
grammar_lessons_naba = [
    {
        "id": 1,
        "titleEn": "Interrogative Particles & Prepositions: عَمَّ (From What?)",
        "titleUr": "حروف استفہام اور حروفِ جر کا امتزاج (عَمَّ)",
        "level": "Beginner",
        "description": "How the question 'About what are they asking?' is constructed grammatically in the opening of Juz 30.",
        "content": "The famous opening word 'عَمَّ' is a combination of two words:\n1. The preposition 'عَنْ' (An - about/regarding).\n2. The interrogative pronoun 'مَا' (Maa - what?).\nWhen 'مَا' is preceded by a preposition (such as عن، في، إلى، بـ), its final Alif is omitted according to Classical Arabic rules: عَنْ + مَا = عَمَّ, فِي + مَا = فِيمَ, إِلَى + مَا = إِلَامَ, بـ + مَا = بِمَ. This grammatical contraction signals urgent inquiry.",
        "surahExamples": [
            {"arabic": "عَمَّ يَتَسَآءَلُونَ", "breakdown": "عَنْ (حرف جر) + مَا (اسم استفهام حذفت ألفه) + يَتَسَآءَلُونَ (فعل مضارع مرفوع بثبوت النون)"},
            {"arabic": "عَنِ ٱلنَّبَإِ ٱلْعَظِيمِ", "breakdown": "عَنِ (حرف جر حرك بالكسر لمنع التقاء الساكنين) + ٱلنَّبَإِ (اسم مجرور) + ٱلْعَظِيمِ (نعت مجرور)"}
        ]
    },
    {
        "id": 2,
        "titleEn": "Dual Objects with 'Ja'ala' (جَعَلَ): Earth & Mountains",
        "titleUr": "افعال القلوب والتحويل: جعل کے دو مفعول",
        "level": "Intermediate",
        "description": "How Allah describes transforming the cosmos for mankind using verbs that take two accusative objects (مفعولين).",
        "content": "The verb 'جَعَلَ' (Ja'ala) in Ayahs 6-11 is a transformative verb (فعل تحويل) requiring two objects in the accusative state (منصوب بالفتحة):\n• أَلَمْ نَجْعَلِ ٱلْأَرْضَ (Object 1: The Earth) مِهَٰدًا (Object 2: A resting bed).\n• وَجَعَلْنَا نَوْمَكُمْ (Object 1: Your sleep) سُبَاتًا (Object 2: A rest/cessation).\n• وَجَعَلْنَا ٱلَّيْلَ (Object 1: The night) لِبَاسًا (Object 2: A garment).\nNotice how every cosmic blessing is balanced with perfect syntactic rhythm.",
        "surahExamples": [
            {"arabic": "أَلَمْ نَجْعَلِ ٱلْأَرْضَ مِهَٰدًا", "breakdown": "نَجْعَلِ (فعل مضارع مجزوم بلم) + ٱلْأَرْضَ (مفعول أول منصوب) + مِهَٰدًا (مفعول ثان منصوب)"},
            {"arabic": "وَجَعَلْنَا نَوْمَكُمْ سُبَاتًا", "breakdown": "جَعَلْنَا (فعل وفاعل) + نَوْمَكُمْ (مفعول أول) + سُبَاتًا (مفعول ثان منصوب)"}
        ]
    },
    {
        "id": 3,
        "titleEn": "Intensifying Verb Patterns: تفعيل (كِذَّابًا) & مبالغة (وَهَّاجًا)",
        "titleUr": "صرفی اوزان: مبالغہ اور مصدرِ تفعيل",
        "level": "Intermediate",
        "description": "The striking, rhythmic rhyming sounds of Surah An-Naba derived from Form II verbal nouns and exaggerated adjectives.",
        "content": "Surah An-Naba is famous for its thunderous, percussive rhyme ending in Alif Tanween (-an):\n1. وَهَّاجًا (Wahhaaj): Pattern 'فَعَّال' (Fa''aal), representing extreme heat, light, and blazing radiance (the Sun).\n2. ثَجَّاجًا (Thajjaaj): Pattern 'فَعَّال', describing torrential, cascading downpours of rain.\n3. كِذَّابًا (Kidhdhaab): Alternative verbal noun of كَذَّبَ (Form II), denoting fierce, vehement rejection and denial of the truth.",
        "surahExamples": [
            {"arabic": "وَجَعَلْنَا سِرَاجًا وَهَّاجًا", "breakdown": "سِرَاجًا (مفعول أول) + وَهَّاجًا (نعت صيغة مبالغة على وزن فَعَّال)"},
            {"arabic": "وَأَنزَلْنَا مِنَ ٱلْمُعْصِرَٰتِ مَآءً ثَجَّاجًا", "breakdown": "مَآءً (مفعول به) + ثَجَّاجًا (نعت صيغة مبالغة لمنصبّ بغزارة)"}
        ]
    },
    {
        "id": 4,
        "titleEn": "The Day of Reckoning: Inna & Its Predicates",
        "titleUr": "حروفِ مشبہ بالفعل اور اس کی خبر (إِنَّ يَوْمَ ٱلْفَصْلِ كَانَ مِيقَٰتًا)",
        "level": "Advanced",
        "description": "Constructing certainty and unshakeable truth with Inna and compound verbal predicates.",
        "content": "Ayah 17 introduces the pivotal transition of Surah An-Naba:\n• 'إِنَّ' (Inna): Particle of certainty that puts its subject into the accusative: 'يَوْمَ' (Yawma - Mansoob).\n• 'ٱلْفَصْلِ' (Al-Fasl): Genitive Mudhaaf Ilayhi (Majroor).\n• The predicate of Inna is the entire sentence: 'كَانَ مِيقَٰتًا' (Kaana Meeqatan) — an appointed, predetermined moment that cannot be delayed.",
        "surahExamples": [
            {"arabic": "إِنَّ يَوْمَ ٱلْفَصْلِ كَانَ مِيقَٰتًا", "breakdown": "إِنَّ (حرف توكيد ونصب) + يَوْمَ (اسم إن منصوب) + ٱلْفَصْلِ (مضاف إليه) + جملة كان ميقاتاً (خبر إن في محل رفع)"}
        ]
    }
]

with open("app/src/main/assets/grammar_naba.json", "w", encoding="utf-8") as f:
    json.dump(grammar_lessons_naba, f, ensure_ascii=False, indent=2)

print("Generated app/src/main/assets/grammar_naba.json.")
