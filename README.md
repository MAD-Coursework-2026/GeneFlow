# GeneFlow — Android Prototype

**Integrated Clinical Genomics and Family Pedigree Tracking Platform**

A Mobile Application Development (MAD) / Final Year Project prototype. A clinician
signs in, manages patients, uploads a VCF (genetic) file, and the app parses the
variants, runs a **from-scratch machine-learning model** to classify them using
ACMG-style rules, and visualises disease risk and family inheritance.

- Language: **Java**
- Build system: **Gradle (Groovy DSL)**
- Database: **SQLite** (on-device, via `SQLiteOpenHelper`)
- ML: **Logistic regression trained from scratch** in pure Java (no pretrained / no Hugging Face)
- Android Studio template: **Empty Views Activity**
- Package: `com.geneflow.app`

---

## 1. Quick start 


**Demo login:** `sana@geneflow.com`  /  password `12345678`
(or tap **Create Account** to register a new clinician).

> The dashboard is seeded with demo patients on first launch, including **Ahmed Khan**
> (BRCA1 Pathogenic, 72% lifetime risk) so every screen has data to show.

---

## 2. File → folder map

Everything lives under `app/src/main/`. Paste each file into the matching folder.

### Manifest
| File | Put it in |
|---|---|
| `AndroidManifest.xml` | `app/src/main/` |

### Gradle (project root)
| File | Put it in |
|---|---|
| `build.gradle` (app module) | `app/` |
| `build.gradle` (project) | project root |
| `settings.gradle` | project root |
| `gradle.properties` | project root |
| `proguard-rules.pro` | `app/` |

> If your generated project uses a different Android Gradle Plugin version, the safest
> option is to keep **your** root `build.gradle`/`settings.gradle` and only copy the
> `dependencies { }`, `compileOptions { }`, and `namespace` lines from the app-level
> `build.gradle` here into yours, then sync.

### Java — `app/src/main/java/com/geneflow/app/`
| File | Sub-folder |
|---|---|
| `MainActivity.java` | (root package) |
| `LoginActivity.java` | (root package) |
| `SignupActivity.java` | (root package) |
| `DashboardActivity.java` | (root package) |
| `PatientRegistrationActivity.java` | (root package) |
| `ProfileViewActivity.java` | (root package) |
| `PedigreeActivity.java` | (root package) |
| `db/DatabaseHelper.java` | `db/` |
| `model/Patient.java`, `Variant.java`, `FamilyMember.java`, `Alert.java`, `User.java` | `model/` |
| `ml/LogisticRegressionModel.java`, `GeneticRiskClassifier.java` | `ml/` |
| `vcf/VcfParser.java` | `vcf/` |
| `adapter/PatientAdapter.java` | `adapter/` |
| `util/Session.java`, `SeedData.java`, `VcfReader.java` | `util/` |

### Resources — `app/src/main/res/`
| File(s) | Sub-folder |
|---|---|
| `activity_main.xml`, `activity_login.xml`, `activity_signup.xml`, `activity_dashboard.xml`, `activity_patient_registration.xml`, `activity_profile_view.xml`, `activity_pedigree.xml`, `item_patient.xml`, `dialog_message.xml`, `dialog_upload_source.xml` | `res/layout/` |
| all `ic_*.xml`, `bg_*.xml`, `status_dot_*.xml` | `res/drawable/` |
| `colors.xml`, `strings.xml`, `themes.xml` | `res/values/` |

### Assets — `app/src/main/assets/`
| File | Folder |
|---|---|
| `sample.vcf` | `app/src/main/assets/` (create the `assets` folder if it isn't there: right-click `main` → New → Folder → Assets Folder) |

---

## 3. Screen map (matches the Figma order)

1. **MainActivity** — landing / splash (Sign in · Create Account)
2. **SignupActivity** — create clinician account
3. **LoginActivity** — sign in
4. **DashboardActivity** — patient list, search, status filters, metrics, recent alerts
5. **PatientRegistrationActivity** — primary patient + multi-member family, upload-source dialog, save/delete dialogs
6. **ProfileViewActivity** — full patient profile, ACMG status, upload new VCF
7. **PedigreeActivity** — family tree, member details, lifetime-risk %

Dialog states from the Figma frames (delete-success, save-success, choose-upload-source)
are implemented as `dialog_message.xml` and `dialog_upload_source.xml`.

---

## 4. How the pieces work

**Database (`db/DatabaseHelper.java`)** — a singleton `SQLiteOpenHelper` with three
tables: `users`, `patients`, `alerts`, and full CRUD. SQLite is the correct choice for a
phone app: it runs entirely on-device with no server.

**Why not MySQL?** MySQL is a *server* database — it needs a running server and a network
connection, so it cannot live inside the app. The standard Android equivalent is SQLite,
which is what is used here. If you later want central/hospital storage, put MySQL behind a
small REST API (e.g. PHP/Node) and have the app sync to it over HTTPS; the on-device
SQLite layer stays as the offline cache.

**VCF parser (`vcf/VcfParser.java`)** — reads raw VCF text, pulls each variant's INFO
fields (allele frequency, REVEL-style score, conservation, ClinVar flag, loss-of-function,
gene), and is written defensively so a malformed line never crashes the app.

**ML model (`ml/`)** — `LogisticRegressionModel` is a logistic-regression classifier
**implemented from scratch**: sigmoid + batch gradient descent over a small hand-authored
synthetic training set of variant features (no external model, no pretrained weights).
`GeneticRiskClassifier` trains it on construction, then for an uploaded VCF picks the most
significant variant, produces an ACMG-style classification (Pathogenic / Likely Pathogenic
/ VUS / Likely Benign / Benign), a status colour, and a lifetime-risk percentage.

**Status colour convention** (the dots on the dashboard and tree):
`0 = red` positive/pathogenic · `1 = grey` no file/pending · `2 = green` negative/benign ·
`3 = blue` uncertain (VUS).

---

## 5. Notes

- All icons are dependency-free **vector drawables** recreated from the design so nothing is
  missing at build time. To use your own exported Figma assets, drop them into
  `res/drawable/` using the same file names.
- The app reads the bundled `assets/sample.vcf` when you tap any **Upload VCF** action, so the
  full analysis pipeline is demonstrable without picking a real file. To wire up a real file
  picker later, use the Storage Access Framework and feed the stream into
  `VcfReader.readFromUri(...)` (already provided).
