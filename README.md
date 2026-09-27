# TURKUAZ AI — Android Tam Sürüm (Faz 2)

Kotlin + Jetpack Compose. Genel kullanıcıya yönelik, sadeleştirilmiş
istemci. Aynı sunucuya (`turkuaz-core`) bağlanır, üretim adresi doğrudan
kodda tanımlı: `https://turkuaz-core2-production.up.railway.app`
(`data/Config.kt`).

## Beta'dan farkları

| | TurkuazBeta | TurkuazFull |
|---|---|---|
| Navigasyon | Alt sekme (4 sekme) | Tek ana ekran (Sohbet) + hamburger menü/drawer |
| Öğret ekranı | Var (manuel öğrenme adayı gönderimi) | **Yok** — sıradan kullanıcı elle "öğrenme adayı" göndermiyor |
| Hafızam | Anahtar/değer formu + dışa aktarma | Sadece günlük dilde liste + Sil |
| Feature flag banner | Var | **Yok** (deneysel özellikler Beta'ya özel, Bölüm 3.1) |
| Geçmiş Sohbetler | Yok | Var (yeni) |
| Hakkımızda | Yok | Var (yeni) |
| "BETA" etiketi | Var | Yok |

## Yeni: Geçmiş Sohbetler

**Önemli mimari not:** Sunucuda (`turkuaz-core`) şu an bir "konuşma/oturum"
kavramı yok — `/api/v1/chat` tek seferlik, durumsuz çalışıyor. Bu yüzden
geçmiş sohbetler özelliği bilinçli olarak **sadece cihaz üzerinde**
(`ChatHistoryStore.kt`, SharedPreferences + JSON, ekstra kütüphane yok)
implemente edildi. Kullanıcı uygulamayı silerse ya da telefon değiştirirse
geçmişi kaybolur. İsterseniz ileride sunucu tarafında kalıcı bir
`Conversation` modeli eklenip bu senkronize hale getirilebilir — şimdilik
istendiği gibi backend'e dokunulmadı.

Üst çubuktaki **+** ikonu yeni bir sohbet başlatır; her mesaj alışverişi
otomatik olarak geçmişe kaydedilir.

## Öğrenme akışı hakkında not

"Öğret" ekranını kaldırdım ama backend'deki `/api/v1/memory/learn-candidate`
ucuna dokunmadım — istendiği gibi. İleride sohbet mesajlarından otomatik
öğrenme adayı tetiklemek isterseniz (ör. kullanıcı bir düzeltme yaptığında)
o mantık hem backend'de (ne zaman tetiklenecek) hem bu istemcide ayrıca
tasarlanmalı; şimdilik hiçbir yerden çağrılmıyor.

## Kayıt (Register) ekranı

Eklendi — `RegisterScreen.kt`. Giriş ekranında "Hesabınız yok mu? Kayıt
olun" bağlantısı bu ekrana götürüyor. Sunucudaki mevcut
`/api/v1/auth/register` ucunu kullanıyor; bu uç zaten bir token döndürdüğü
için başarılı kayıttan sonra ayrıca giriş yapmaya gerek yok, doğrudan ana
ekrana geçiliyor. Küçük bir istemci-tarafı ekleme: şifre en az 6 karakter
olmadan "Kayıt Ol" butonu aktif olmuyor (sunucu tarafında böyle bir kural
yok, sadece temel bir UX önlemi — isterseniz kaldırabiliriz).

## GitHub Actions APK derlemesi

`.github/workflows/build.yml` eklendi — push/PR'da otomatik debug APK
derleyip 30 gün saklıyor.

**Not:** TurkuazBeta'da zaten kendi (ayrı, manuel eklenmiş) bir GitHub
Actions dosyası var — burada onu değiştirmedim, sadece TurkuazFull için
kendi mantığıyla ayrı bir tane kurdum.

## Test durumu

Aynı kısıt geçerli: bu sandbox'ta Android SDK/internet olmadığı için gerçek
bir Gradle derlemesi yapılamadı — kod elle gözden geçirildi, parantez
dengesi otomatik kontrol edildi. İlk gerçek derlemeyi Android Studio'da ya
da GitHub Actions'ta göreceksiniz.
