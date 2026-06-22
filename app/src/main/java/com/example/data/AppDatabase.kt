package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [UserProfile::class, HourCorrespondence::class, LunarMansion::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun hourCorrespondenceDao(): HourCorrespondenceDao
    abstract fun lunarMansionDao(): LunarMansionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "al_buni_clock_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database)
                }
            }
        }

        private suspend fun populateDatabase(db: AppDatabase) {
            // Seed 28 Lunar Mansions
            val mansions = getLunarMansionsList()
            db.lunarMansionDao().insertAll(mansions)

            // Seed 168 Hour Correspondences
            val correspondences = getHourCorrespondencesList()
            db.hourCorrespondenceDao().insertAll(correspondences)
        }

        private fun getLunarMansionsList(): List<LunarMansion> {
            val names = listOf(
                "الشرطين", "البطين", "الثريا", "الدبران", "الهقعة", "الهنعة", "الذراع",
                "النثرة", "الطرف", "الجبهة", "الزبرة", "الصرفة", "العواء", "السماك",
                "الغفر", "الزبانا", "الإكليل", "القلب", "الشولة", "النعائم", "البلدة",
                "سعد الذابح", "سعد بلع", "سعد السعود", "سعد الأخبية", "الفرع المقدم", "الفرع المؤخر", "رشاء (بطن الحوت)"
            )
            val signs = listOf(
                "الحمل", "الحمل", "الثور", "الثور", "الجوزاء", "الجوزاء", "السرطان",
                "السرطان", "الأسد", "الأسد", "الأسد", "العذراء", "العذراء", "الميزان",
                "الميزان", "العقرب", "العقرب", "العقرب", "القوس", "القوس", "القوس",
                "الجدي", "الجدي", "الدلو", "الدلو", "الحوت", "الحوت", "الحوت"
            )
            val elements = listOf(
                "ناري", "ناري", "ترابي", "ترابي", "هوائي", "هوائي", "مائي",
                "مائي", "ناري", "ناري", "ناري", "ترابي", "ترابي", "هوائي",
                "هوائي", "مائي", "مائي", "مائي", "ناري", "ناري", "ناري",
                "ترابي", "ترابي", "هوائي", "هوائي", "مائي", "مائي", "مائي"
            )
            val natures = listOf(
                "سعيد للخير والمحبات", "ممتزج للأعداد والأوفاق", "سعيد جداً للثراء", "نحس تجنب التسرع",
                "ممتزج للتحصيل العلمي", "سعيد لجمع الخصوم", "سعيد لاستجابة الدعاء", "نحس يكره فيه السفر والزواج",
                "ممتزج للحذر والحيطة", "سعيد عظيم للجاه والتمكين", "ممتزج للبناء والعمل الصادق", "ممتزج لتثبيت العقود",
                "سعيد جداً لجلب الأرزاق", "نحس تجنب العداوات والنزاع", "سعيد جداً للأسرار الباطنية", "نحس يكره فيه المغامرة",
                "سعيد مبارك لفك الكروب", "ممتزج للتحصين والأوراد", "نحس للدفاع ودفع الظلم", "سعيد عظيم للعبادات والطاعة",
                "سعيد للسفر والتشييد الجديد", "نحس للعزلة والمراجعة الفكرية", "ممتزج للأدوية والعلوم الطبية", "سعيد عظيم للمحبة والغنى",
                "ممتزج لكشف الخبايا والكنوز", "سعيد مبارك لقضاء حوائج المعاش", "ممتزج للكتابة والتدبر اللطيف", "سعيد مبارك للبركة المستدامة والهيبة"
            )
            val meanings = listOf(
                "قرنا الحمل الحاميان الفاتحان لطريق النور.",
                "البطن الداخلي الحافظ لطاقات النشوء والتأسيس الحكيم.",
                "عنقود الكواكب اللامع، مفتاح البركة الكوزمية العظمى.",
                "صاحب الجدية والالتزام، موضع اختبار الصبر والثبات.",
                "دائرة العلوم وجوهر البحث العقلي وطلب المعرفة والبيان.",
                "موضع التقارب والألفة والمواثيق والصلح ومحبة العشائر.",
                "ذراع الاستجابة والدعوات السامعة من العرش الحكيم.",
                "فجوة الصبر والدروس الصعبة، تجنب الخطوات السريعة فيها.",
                "طَرَف العين الفطن، يورث مراقبة الأعداء والاستعداد.",
                "جبهة الأسد الشامخة، تاج السيادة والولاية والجاه العظيم.",
                "الكتف والمظهر القوي، مدعاة للتأسيس المتين والبناء الراسخ.",
                "الانصراف والتحول الإيجابي، فرصة للحصاد والزراعة المثمرة.",
                "العواء والنداء الروحاني، جلب حوائج القلوب وتنمية المحبة السريعة.",
                "قبة الأعزال البعيدة عن الفتن، تتطلب حفظ السر والابتعاد عن الخلاف.",
                "ستار الغفران واللطف، ساعة السماع الرباني والتأمل الرفيع.",
                "موضعان متباعدان، موضع تفكر وتيقظ شديد بالروح.",
                "إكليل النصر والهيبة الحرة، تفتح فيها أبواب النصر وقهر الباطل.",
                "قلب العقرب المشتعل بالهيبة والحضور، يصلح للتحصين الأكبر.",
                "إبرة الشوكة المانعة، تستعمل لقطع الأذى والشرور والتحوط التام.",
                "الأنعام المباركة الوافرة، مستجاب فيها الصلوات الكبرى والفتح المبين.",
                "البلد الشاسع والتمكين الأرضي، مناسب لقضاء حوائج الرزق والمعاش.",
                "صبر الذابح الحاسم، موزانة قاطعة للأمور المائعة.",
                "ابتلاع الطاقات وحفظها للشفاء البدني والروحاني الأوفق.",
                "سعد العوالم كلها وجلب الأفراح والبركات والقبول الإلهي الحارس.",
                "مخزن الأسرار المكنونة وفك الطلاسم الحياتية واستخراج العلوم الغامضة.",
                "المقدم المبشر بالخير والبركات المادية والحلول الروحانية السريعة.",
                "المؤخر المتمم للأعمال الروحانية الطيبة والدراسات الروحية العالية.",
                "ذيل السمكتين الملتف بالصبر والحفظ الإلهي الباقي الدائم."
            )
            val actions = listOf(
                "تلو الأوراد النارية وجلب المحبة الشريفة والتواصل الإيجابي.",
                "الرياضات الرياضية وكتابة الوفق الشريف للأعداد وعلوم الحروف.",
                "كتابة حجاب القبول الأعظم والبدء بتجارة كبرى وطلب الفتح المادي.",
                "العزلة الكاملة والتحصين وقراءة آية الكرسي مكررة لدفع النحوسات.",
                "مطالعة متون العلم، كتابة الحجج وحل المسائل العلمية الصعبة.",
                "زيارة الأهل وعقد عهود الصداقة والتحكيم بين المخاصمين بالحب.",
                "صلاة الفرج، كست اللبان والدعاء بأسرار الاسم الأعظم لله الجليل.",
                "التحرز وقراءة المعوذات والابتعاد عن مواطن الجدل والنفير.",
                "التخطيط الاستراتيجي ومراقبة النفس ودراسة السوق والمستجدات.",
                "عقد الاجتماعات السياسية، طلب ترقية، وكتابة أوفاق الهيبة والرفعة.",
                "صب الأساسات العمرانية وتأسيس الشركات والمشروعات طويلة الأمد.",
                "كتابة عقود الاستئجار والبيع والشراء وتثبيت المواصفات الزراعية.",
                "تأليف القلوب، الاستمالة بالخير، وعقد الوفاق الزوجي المبارك.",
                "تجنب الدخول في نزاعات، والابتعاد عن أماكن الشر والتحرز الصادق.",
                "رياضة روحانية خفيفة، والتضرع بالأسماء اللطيفة لنيل القبول الأكبر.",
                "قراءة سورة يس لحفظ النفس من المكائد الكامنة والتزام الهدوء.",
                "عقد الصفقات الكبرى وتوجيه الرأي العام وإبراز مواطن القوة بالحق.",
                "التحصين بأقسام الحفظ، وقراءة أذكار المساء والصباح بعناية.",
                "صرف الطاقات السلبية وتطهير المنازل وإخراج الصدقات المانعة للبلاء.",
                "التوجه للقبلة والذكر بأسماء الفتاح والوهاب لنيل الفيوضات الربانية.",
                "الأسفار البرية والبحرية وبناء المنازل وتوسيع رقعة النفوذ العادلة.",
                "الاختلاء بالذات ومجاهدة النفس بالأوراد والتزام الصمت المريح لـ 60 دقيقة.",
                "كتابة أحراز الصحة العامة الشفاء من الأسقام والعلل العارضية.",
                "السعي وراء طلب الوظائف وتوطيد الصداقات وبدء مشاريع ضخمة ببركة تامة.",
                "الكشف الروحي الشفاف وتحليل الرمل وكتابة الجداول الحكيمة في السحر الحلال.",
                "محادثة ذوي القرار وحل الأزمات العالقة في وظيفتك أو معاملتك الحالية.",
                "المطالعة الفكرية العميقة وكتابة الوثائق وحفظ الأوراق الثمينة.",
                "تأسيس مشروعات تجارية كبرى والزواج المبارك وعقد محبة باقية بالخير."
            )

            val step = 360.0 / 28.0
            return List(28) { i ->
                LunarMansion(
                    index = i + 1,
                    name = names[i],
                    degreeStart = i * step,
                    degreeEnd = (i + 1) * step,
                    zodiacSign = signs[i],
                    element = elements[i],
                    nature = natures[i],
                    meanings = meanings[i],
                    actions = actions[i]
                )
            }
        }

        private fun getHourCorrespondencesList(): List<HourCorrespondence> {
            val planetsList = listOf(
                // 0: زحل
                PlanetInfo(
                    arabicName = "زحل",
                    planetSpiritual = "كوكب الأسرار والمكابدات والباطن والأمور الجادة",
                    angelName = "كسفيائيل",
                    prophetName = "سليمان عليه السلام",
                    divineName = "يا قيوم يا رحمن يا مقتدر",
                    letters = "ج ش ث ف ض",
                    incense = "ميعة رفيعة، قشر برنجاس وصندل داكن",
                    color = "أسود فاحم كحلي داكن",
                    temperament = "بارد يابس قابض",
                    direction = "شمال",
                    positiveWorks = "الرياضات الروحانية العميقة، حفر الآبار والقنوات واستخراج الدفائن وتحصين الأماكن.",
                    caution = "تجنب المعاملات المالية السريعة وعقد النكاح أو البدء بالأسفار المستعجلة لغلبة طبع القبض الصارم."
                ),
                // 1: مشتري
                PlanetInfo(
                    arabicName = "المشتري",
                    planetSpiritual = "كوكب السعد الأكبر والأحكام الرفيعة والوفرة المطلقة",
                    angelName = "صرفيائيل",
                    prophetName = "إبراهيم عليه السلام",
                    divineName = "يا عظيم يا كبير يا حي يا وهاب",
                    letters = "د خ ظ ج ز",
                    incense = "عود كمبودي رفيع ولبان سقطري عذب",
                    color = "أرجواني بوربون وأزرق ملكي سماوي",
                    temperament = "حار رطب ميمون سعيد جداً",
                    direction = "شرق",
                    positiveWorks = "طلب الحوائج من أولي الأمر، عهود النكاح، الصدقات الكبرى، وكتابة الأوفاق الشريفة للأرزاق الجارية.",
                    caution = "ساعة ميمونة طاهرة من كل شائبة مكروهة، ولا محاذير فيها إلا أن تضمر سوءاً أو شركاً."
                ),
                // 2: مريخ
                PlanetInfo(
                    arabicName = "المريخ",
                    planetSpiritual = "كوكب القهر والسطوة وبهرام المعارك وقطع الحجج الشديدة",
                    angelName = "سمسمائيل",
                    prophetName = "داود عليه السلام",
                    divineName = "يا جبار يا عزيز يا متكبر يا قاهر",
                    letters = "هـ ذ غ ط ص",
                    incense = "قسط هندي أحمر وحرمل بري سندروس",
                    color = "أحمر قاني وكرمزي ناري",
                    temperament = "حار يابس قاهر شديد وافر الأثر",
                    direction = "جنوب",
                    positiveWorks = "دفع الأعداء والمارقين، قطع كيد المعتدين بالصدق بالآيات الروحانية وقراءة سور العز والتحصين الشديد.",
                    caution = "اجتنب الأعمال المتعلقة بالوداد أو الصلح، ولا تباشر جراحة بدنية ولا تجادل الأقارب أو الأصدقاء."
                ),
                // 3: شمس
                PlanetInfo(
                    arabicName = "الشمس",
                    planetSpiritual = "كوكب النور المحيط وجلال الملك والسيادة والجاه العظيم",
                    angelName = "روقيائيل",
                    prophetName = "إدريس عليه السلام",
                    divineName = "يا نور يا الله يا حي يا قيوم يا هادي",
                    letters = "أ هـ ط م س",
                    incense = "مصطكى يوناني، صندل أصفر ذهبي وجاوى رفيع",
                    color = "أصفر ذهبي وضياء ساطع",
                    temperament = "حار يابس معتدل عظيم الحضور والجاه",
                    direction = "شرق",
                    positiveWorks = "مقابلة الأمراء وأصحاب القرار، طلب الترقي ومكانة العز والشرف، وعهود الولاية وصناعة خواتم القبول الساطعة.",
                    caution = "احذر الكبر والمباهاة والغرور بالنعم فإنه يفسد الفيض الروحاني ويسلب عواقب العمل الطيبة سريعة الزوال."
                ),
                // 4: زهرة
                PlanetInfo(
                    arabicName = "الزهرة",
                    planetSpiritual = "كوكب المحبة والجمال والألفة الخالصة والبهجة الرفيعة",
                    angelName = "عنيائيل",
                    prophetName = "يوسف عليه السلام",
                    divineName = "يا ودود يا لطيف يا كافي يا معطي",
                    letters = "و ض غ ك ي",
                    incense = "لبان ذكر هندي، كافور ومستكة ناعمة وعود هندي فواح",
                    color = "أخضر زمردي بهيج ممتزج بوردي",
                    temperament = "بارد رطب مبهج ميمون السحب",
                    direction = "غرب",
                    positiveWorks = "الخطبة وعقد النكاح المبارك، تودد المخاصمين، تجارة العطور والجواهر والملابس وقبائل المحبات الكبرى.",
                    caution = "احذر شهوات النفس والبطالة والتهاون بالأعمال الجادة وتجنب النميمة والفساد فإن عاقبتها ندامة بليغة."
                ),
                // 5: عطارد
                PlanetInfo(
                    arabicName = "عطارد",
                    planetSpiritual = "كوكب ديوان الكتابة والفهم والتعبير والحكمة والمسائل العقلية",
                    angelName = "ميكائيل",
                    prophetName = "موسى عليه السلام",
                    divineName = "يا حكيم يا عليم يا هادي يا حفيظ",
                    letters = "ز ل م ن ق",
                    incense = "صندل أبيض عتيق وصمغ عربي ومصطكى هندي",
                    color = "فيروزي صافٍ ممزوج بالرمادي اللؤلؤي",
                    temperament = "معتدل يابس حكيم فطن متقلب المزاج الحاكم",
                    direction = "غرب",
                    positiveWorks = "كتابة المصنفات، دراسة العلوم العقلية، توقيع الشراكات التجارية، إرسال المراسلات والتحوط الوفقي اللطيف.",
                    caution = "تجنب التلفظ بالكذب أو المبالغة السامجة في مجالسها، واحذر عقد العقود لمن طَبْعه هوائي لا يثبت على عهد."
                ),
                // 6: قمر
                PlanetInfo(
                    arabicName = "القمر",
                    planetSpiritual = "كوكب التنقلات والتكاثر والأسرار المائية والسرعة البرجية",
                    angelName = "جبرائيل",
                    prophetName = "يونس عليه السلام",
                    divineName = "يا لطيف يا قدوس يا سلام يا باسط",
                    letters = "ح ص ق ر ت",
                    incense = "لبان هندي مر وكافور أبيض وصندل أبيض ناعم",
                    color = "فضي مشع مع بياض ناصع مكسو بنور خفيف",
                    temperament = "بارد رطب متقلب سريع المرور بالأبرجة",
                    direction = "شمال",
                    positiveWorks = "السفر وتجارة المياه والزراعة وسرعة الحركة، معالجة الروح بالدواء الطبيعي والمياه الرقيا المباركة ونفض الدنس.",
                    caution = "تجنب إرساء أسس طويلة الزمن كبناء الحصون والعمائر العظمى لأن القمر سريع التقلب فتتداعى ثوابتها."
                )
            )

            val startIndices = intArrayOf(3, 6, 2, 5, 1, 4, 0)
            val list = mutableListOf<HourCorrespondence>()

            for (day in 0..6) {
                val startIndex = startIndices[day]
                for (hour in 1..24) {
                    val planetIndex = (startIndex + (hour - 1)) % 7
                    val planet = planetsList[planetIndex]
                    list.add(
                        HourCorrespondence(
                            dayOfWeek = day,
                            hourNumber = hour,
                            planetArabic = planet.arabicName,
                            planetSpiritual = planet.planetSpiritual,
                            angelName = planet.angelName,
                            prophetName = planet.prophetName,
                            divineName = planet.divineName,
                            letters = planet.letters,
                            incense = planet.incense,
                            color = planet.color,
                            temperament = planet.temperament,
                            direction = planet.direction,
                            positiveWorks = planet.positiveWorks,
                            caution = planet.caution
                        )
                    )
                }
            }
            return list
        }
    }
}

// Compact helper to hold seed arrays without pollution
private data class PlanetInfo(
    val arabicName: String,
    val planetSpiritual: String,
    val angelName: String,
    val prophetName: String,
    val divineName: String,
    val letters: String,
    val incense: String,
    val color: String,
    val temperament: String,
    val direction: String,
    val positiveWorks: String,
    val caution: String
)
