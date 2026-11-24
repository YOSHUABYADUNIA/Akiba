# Akiba — Application de Gestion de Tontines (Ikibiri / Likilemba)

**Akiba** est une application Android **Offline-First** (fonctionnant sans connexion Internet avec synchronisation automatique vers **Supabase**), conçue pour les groupes d'épargne communautaire (*Ikibiri* au Burundi, *Likilemba* en RDC, et tontines en Afrique).

## Contenu du projet
- `supabase/schema.sql` : Script SQL complet à copier/coller dans Supabase (tables, index, triggers `updated_at` et RLS).
- `android/` : Code source complet Android (Kotlin + Jetpack Compose + Room SQLite + Supabase SDK).
- Support multilingue intégré : **Français** (`values`), **Kirundi** (`values-rn`), **Swahili** (`values-sw`).
- Générateur de reçus partageables directement sur **WhatsApp / SMS**.

## Démarrage rapide
1. **Supabase** : Créez un projet sur [supabase.com](https://supabase.com), ouvrez **SQL Editor**, collez `supabase/schema.sql` et cliquez sur **Run**.
2. **Android Studio** : Ouvrez le dossier `android/`, éditez `SupabaseConfig.kt` avec votre `Project URL` et votre clé `anon public`.
3. **GitHub** : Poussez l'ensemble du dossier sur votre dépôt GitHub.
