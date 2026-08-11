# Email routing — eHealth

Configurer dans Cloudflare → **Compute → Email Service → Email Routing → Routing Rules** pour `optimizesolux.com`.

Chaque règle : **Send to an email** → `francis.ahonsou@gmail.com` (déjà vérifié).

| Adresse | Usage |
|---------|--------|
| `ehealth@optimizesolux.com` | Contact marque (affiché footer) |
| `contact.ehealth@optimizesolux.com` | Contact produit (optionnel) |
| `support.ehealth@optimizesolux.com` | Support (routé, non affiché sur le site) |

> Produit self-serve (modèle CleanTrack) : pas de formulaire « demande de démo ».  
> Les visiteurs créent un compte (plan Free) ou se connectent.

Tester depuis une boîte **autre** que la destination Gmail.
