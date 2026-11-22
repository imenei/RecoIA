import { Component, inject, signal } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { NIVEAUX } from '../../core/mock-courses';
import { Icon } from '../../shared/icon/icon';

const API = 'http://localhost:8082';

interface AdminCourse {
  id?: number;
  title: string;
  university: string | null;
  level: string | null;
  category: string | null;
  description: string | null;
  skills: string | null;
  rating: number;
  reviews: number;
  hours: number;
  imageUrl: string | null;
  badge: string | null;
}
interface PageResponse {
  content: AdminCourse[];
  page: { size: number; number: number; totalElements: number; totalPages: number };
}
type Draft = Record<'title' | 'university' | 'level' | 'category' | 'hours' | 'description' | 'skills' | 'badge', string>;

const EMPTY: Draft = {
  title: '', university: '', level: 'Licence', category: 'Informatique',
  hours: '20', description: '', skills: '', badge: '',
};

@Component({
  selector: 'app-admin-page',
  imports: [Icon],
  template: `
    <section class="head">
      <div>
        <h1>Administration</h1>
        <p>Gérez le catalogue : ajoutez, modifiez ou supprimez des formations, envoyez des images et importez un fichier.</p>
      </div>
    </section>

    @if (notice()) {
      <p class="banner ok" role="status">{{ notice() }} <button type="button" (click)="notice.set('')" aria-label="Fermer"><app-icon name="x" [size]="16" /></button></p>
    }
    @if (error()) {
      <p class="banner ko" role="alert">{{ error() }} <button type="button" (click)="error.set('')" aria-label="Fermer"><app-icon name="x" [size]="16" /></button></p>
    }

    <section class="card box">
      <div class="bar">
        <label class="search">
          <app-icon name="search" [size]="20" />
          <input #s type="search" placeholder="Rechercher une formation…" (input)="onSearch(s.value)" />
        </label>
        <input #csv type="file" accept=".csv,text/csv" hidden (change)="importCsv(csv.files); csv.value = ''" />
        <button type="button" class="btn light" [disabled]="importing()" (click)="csv.click()">
          <app-icon name="upload" [size]="18" /> {{ importing() ? 'Import…' : 'Importer un CSV' }}
        </button>
        <button type="button" class="btn" (click)="openNew()"><app-icon name="plus" [size]="18" /> Ajouter une formation</button>
      </div>
      <p class="hint">Colonnes du CSV : title, university, level, category, description, skills, rating, reviews, hours. Le format du dataset Coursera est aussi accepté.</p>

      @if (loading()) {
        <p class="empty">Chargement…</p>
      } @else if (items().length === 0) {
        <p class="empty">Aucune formation trouvée.</p>
      } @else {
        <div class="scroll">
          <table>
            <thead>
              <tr><th></th><th>Formation</th><th>Catégorie</th><th>Niveau</th><th>Durée</th><th>Note</th><th class="end">Actions</th></tr>
            </thead>
            <tbody>
              @for (c of items(); track c.id) {
                <tr>
                  <td>
                    @if (img(c)) { <img class="thumb" [src]="img(c)" alt="" /> }
                    @else { <span class="thumb none"><app-icon name="image" [size]="20" /></span> }
                  </td>
                  <td><b>{{ c.title }}</b><span class="uni">{{ c.university }}</span></td>
                  <td>{{ c.category }}</td>
                  <td>{{ c.level }}</td>
                  <td>{{ c.hours }} h</td>
                  <td>★ {{ c.rating }}</td>
                  <td class="end">
                    <button type="button" class="icon" (click)="openEdit(c)" [attr.aria-label]="'Modifier ' + c.title"><app-icon name="edit" [size]="18" /></button>
                    <button type="button" class="icon danger" (click)="remove(c)" [attr.aria-label]="'Supprimer ' + c.title"><app-icon name="trash" [size]="18" /></button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
        <div class="pager">
          <span>{{ total() }} formations</span>
          <div>
            <button type="button" class="btn light small" [disabled]="page() === 0" (click)="go(page() - 1)">Précédent</button>
            <span class="pg">Page {{ page() + 1 }} / {{ totalPages() }}</span>
            <button type="button" class="btn light small" [disabled]="page() + 1 >= totalPages()" (click)="go(page() + 1)">Suivant</button>
          </div>
        </div>
      }
    </section>

    @if (formOpen()) {
      <div class="overlay" (click)="closeForm()" (keyup.escape)="closeForm()" tabindex="-1">
        <div class="modal" role="dialog" aria-modal="true" aria-labelledby="dlg-title" (click)="$event.stopPropagation()" (keyup.escape)="closeForm()" tabindex="-1">
          <h2 id="dlg-title">{{ editing() ? 'Modifier la formation' : 'Ajouter une formation' }}</h2>

          <label>Titre *
            <input #t type="text" [value]="draft().title" (input)="set('title', t.value)" maxlength="300" />
          </label>
          <label>Université
            <input #u type="text" [value]="draft().university" (input)="set('university', u.value)" maxlength="200" />
          </label>
          <div class="two">
            <label>Niveau
              <select #lv (change)="set('level', lv.value)">
                @for (n of niveaux; track n) { <option [value]="n" [selected]="draft().level === n">{{ n }}</option> }
              </select>
            </label>
            <label>Catégorie
              <select #ca (change)="set('category', ca.value)">
                @for (n of categories; track n) { <option [value]="n" [selected]="draft().category === n">{{ n }}</option> }
              </select>
            </label>
          </div>
          <div class="two">
            <label>Durée (heures)
              <input #h type="number" min="0" max="1000" [value]="draft().hours" (input)="set('hours', h.value)" />
            </label>
            <label>Badge (facultatif)
              <input #b type="text" [value]="draft().badge" (input)="set('badge', b.value)" maxlength="100" placeholder="ex. Nouveau" />
            </label>
          </div>
          <label>Description
            <textarea #d rows="3" [value]="draft().description" (input)="set('description', d.value)" maxlength="4000"></textarea>
          </label>
          <label>Compétences (séparées par des virgules)
            <input #k type="text" [value]="draft().skills" (input)="set('skills', k.value)" maxlength="1000" />
          </label>

          <div class="imgrow">
            @if (preview()) { <img class="thumb big" [src]="preview()" alt="Aperçu de l’image" /> }
            @else { <span class="thumb big none"><app-icon name="image" [size]="28" /></span> }
            <div>
              <input #f type="file" accept="image/jpeg,image/png,image/webp" hidden (change)="pickFile(f.files)" />
              <button type="button" class="btn light small" (click)="f.click()"><app-icon name="upload" [size]="16" /> Choisir une image</button>
              <p class="hint">JPG, PNG ou WebP, 5 Mo maximum.</p>
            </div>
          </div>

          @if (formError()) { <p class="banner ko" role="alert">{{ formError() }}</p> }

          <div class="actions">
            <button type="button" class="btn light" (click)="closeForm()">Annuler</button>
            <button type="button" class="btn" [disabled]="saving()" (click)="save()">{{ saving() ? 'Enregistrement…' : 'Enregistrer' }}</button>
          </div>
        </div>
      </div>
    }
  `,
  styles: `
    :host { display: block; }
    h1 { font-size: 34px; }
    .head { margin: 22px 0 18px; }
    .head p { margin: 8px 0 0; color: var(--ink); }
    .box { padding: 20px 22px 24px; border-radius: 26px; display: grid; gap: 14px; }
    .bar { display: flex; flex-wrap: wrap; gap: 12px; align-items: center; }
    .search { flex: 1; min-width: 220px; display: flex; align-items: center; gap: 10px; height: 48px; padding: 0 18px;
      border-radius: 999px; background: #F2FCFD; color: var(--primary-dark); box-shadow: inset 0 0 0 1px #D8F6F8; }
    .search input { flex: 1; border: 0; outline: 0; background: transparent; font: 15px 'Inter', sans-serif; color: var(--ink); }
    .btn { display: inline-flex; align-items: center; gap: 8px; height: 48px; padding: 0 20px; border: 0; border-radius: 999px;
      background: #0E9FB0; color: #fff; font: 600 14px 'Inter', sans-serif; cursor: pointer; }
    .btn.light { background: #fff; color: var(--ink); box-shadow: inset 0 0 0 1.5px #BFEFF4; }
    .btn.small { height: 38px; padding: 0 14px; font-size: 13px; }
    .btn:disabled { opacity: .55; cursor: not-allowed; }
    .hint { margin: 0; font-size: 12.5px; color: var(--muted); }
    .banner { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin: 0 0 14px; padding: 10px 14px; border-radius: 12px; font-size: 14px; }
    .banner.ok { background: #E3F8EC; color: #11613A; }
    .banner.ko { background: #FEECEC; color: #B42318; }
    .banner button { border: 0; background: none; color: inherit; cursor: pointer; display: inline-flex; }
    .empty { text-align: center; padding: 36px 0; color: var(--muted); }
    .scroll { overflow-x: auto; }
    table { width: 100%; border-collapse: collapse; font-size: 14px; }
    th { text-align: left; font-size: 12px; text-transform: uppercase; letter-spacing: .04em; color: var(--muted); padding: 8px 10px; }
    td { padding: 10px; border-top: 1px solid #E5F6F8; vertical-align: middle; }
    .end { text-align: right; white-space: nowrap; }
    .uni { display: block; font-size: 12.5px; color: var(--muted); }
    .thumb { width: 56px; height: 40px; border-radius: 8px; object-fit: cover; background: #BFEFF4; display: inline-grid; place-items: center; color: var(--primary-dark); }
    .thumb.big { width: 120px; height: 80px; }
    .icon { border: 0; background: none; color: var(--ink); cursor: pointer; padding: 6px; border-radius: 8px; }
    .icon:hover { background: var(--primary-light); }
    .icon.danger { color: #B42318; }
    .icon.danger:hover { background: #FEECEC; }
    .pager { display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 10px; font-size: 14px; color: var(--muted); }
    .pg { margin: 0 10px; color: var(--ink); }
    .overlay { position: fixed; inset: 0; background: rgba(11,44,69,.45); display: grid; place-items: center; padding: 20px; z-index: 50; overflow-y: auto; }
    .modal { width: min(620px, 100%); background: #fff; border-radius: 24px; padding: 26px 28px; display: grid; gap: 12px; box-shadow: 0 20px 60px rgba(11,44,69,.3); }
    .modal h2 { font-size: 22px; margin: 0 0 4px; }
    .modal label { display: grid; gap: 6px; font-size: 13px; font-weight: 600; color: var(--ink); }
    .modal input[type=text], .modal input[type=number], .modal select, .modal textarea {
      border: 0; border-radius: 12px; padding: 11px 14px; font: 15px 'Inter', sans-serif; color: var(--ink);
      background: #fff; box-shadow: inset 0 0 0 1.5px #D3F1F5; font-weight: 400; }
    .modal input:focus, .modal select:focus, .modal textarea:focus { outline: 3px solid var(--primary); outline-offset: 0; }
    .two { display: grid; grid-template-columns: 1fr 1fr; gap: 12px; }
    .imgrow { display: flex; align-items: center; gap: 16px; }
    .actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 4px; }
    button:focus-visible { outline: 3px solid var(--primary-dark); outline-offset: 2px; }
    @media (max-width: 700px) { .two { grid-template-columns: 1fr; } h1 { font-size: 26px; } }
  `,
})
export class AdminPage {
  private http = inject(HttpClient);

  readonly niveaux = NIVEAUX;
  // memes categories que l'inscription et le catalogue
  readonly categories = ['Informatique', 'Data Science', 'Business', 'Langues', 'Santé', 'Arts et design', 'Ingénierie'];
  readonly pageSize = 10;

  items = signal<AdminCourse[]>([]);
  page = signal(0);
  totalPages = signal(0);
  total = signal(0);
  q = signal('');
  loading = signal(true);
  notice = signal('');
  error = signal('');

  formOpen = signal(false);
  editing = signal<AdminCourse | null>(null);
  draft = signal<Draft>({ ...EMPTY });
  file = signal<File | null>(null);
  preview = signal('');
  formError = signal('');
  saving = signal(false);
  importing = signal(false);

  private blobUrl = '';
  private timer: ReturnType<typeof setTimeout> | undefined;

  constructor() { this.load(); }

  // ---------------------------------------------------------------- liste
  load() {
    this.loading.set(true);
    this.http.get<PageResponse>(`${API}/api/courses`, {
      params: { q: this.q(), page: this.page(), size: this.pageSize, sort: 'id,desc' },
    }).subscribe({
      next: r => {
        this.items.set(r.content);
        this.totalPages.set(r.page.totalPages);
        this.total.set(r.page.totalElements);
        this.loading.set(false);
      },
      error: e => { this.loading.set(false); this.error.set(this.msg(e)); },
    });
  }

  onSearch(v: string) {
    clearTimeout(this.timer);
    this.timer = setTimeout(() => { this.q.set(v.trim()); this.page.set(0); this.load(); }, 250);
  }

  go(p: number) { this.page.set(Math.max(0, p)); this.load(); }

  img(c: AdminCourse): string {
    if (!c.imageUrl) return '';
    return c.imageUrl.startsWith('http') ? c.imageUrl : API + c.imageUrl;
  }

  // ------------------------------------------------------------- formulaire
  set(field: string, value: string) { this.draft.update(d => ({ ...d, [field]: value })); }

  openNew() {
    this.resetPreview();
    this.editing.set(null);
    this.draft.set({ ...EMPTY });
    this.file.set(null);
    this.formError.set('');
    this.formOpen.set(true);
  }

  openEdit(c: AdminCourse) {
    this.resetPreview();
    this.editing.set(c);
    this.draft.set({
      title: c.title ?? '',
      university: c.university ?? '',
      level: c.level ?? 'Licence',
      category: c.category ?? 'Informatique',
      hours: String(c.hours ?? 0),
      description: c.description ?? '',
      skills: c.skills ?? '',
      badge: c.badge ?? '',
    });
    this.file.set(null);
    this.preview.set(this.img(c));
    this.formError.set('');
    this.formOpen.set(true);
  }

  closeForm() {
    this.resetPreview();
    this.formOpen.set(false);
  }

  pickFile(files: FileList | null) {
    const f = files?.item(0);
    if (!f) return;
    if (!['image/jpeg', 'image/png', 'image/webp'].includes(f.type)) {
      this.formError.set('Format refusé : utilisez JPG, PNG ou WebP.');
      return;
    }
    if (f.size > 5 * 1024 * 1024) {
      this.formError.set('Image trop lourde (5 Mo maximum).');
      return;
    }
    this.formError.set('');
    this.resetPreview();
    this.blobUrl = URL.createObjectURL(f);
    this.file.set(f);
    this.preview.set(this.blobUrl);
  }

  private resetPreview() {
    if (this.blobUrl) { URL.revokeObjectURL(this.blobUrl); this.blobUrl = ''; }
    this.preview.set('');
  }

  save() {
    const d = this.draft();
    const hours = Number(d.hours);
    if (!d.title.trim()) { this.formError.set('Le titre est obligatoire.'); return; }
    if (!Number.isFinite(hours) || hours < 0 || hours > 1000) { this.formError.set('Durée invalide (0 à 1000 heures).'); return; }

    this.saving.set(true);
    this.formError.set('');
    const base = this.editing();
    const isEdit = base?.id != null;
    // PUT remplace toute la formation : on renvoie aussi note, avis et image existants
    const body: AdminCourse = {
      ...(base ?? { rating: 0, reviews: 0, imageUrl: null }),
      title: d.title.trim(),
      university: d.university.trim() || null,
      level: d.level,
      category: d.category,
      description: d.description.trim() || null,
      skills: d.skills.trim() || null,
      badge: d.badge.trim() || null,
      hours: Math.round(hours),
    };
    const req = isEdit
      ? this.http.put<AdminCourse>(`${API}/api/courses/${base!.id}`, body)
      : this.http.post<AdminCourse>(`${API}/api/courses`, body);

    req.subscribe({
      next: saved => {
        const f = this.file();
        if (f && saved.id != null) this.upload(saved.id, f, isEdit);
        else this.afterSave(isEdit ? 'Formation modifiée.' : 'Formation ajoutée.', isEdit);
      },
      error: e => { this.saving.set(false); this.formError.set(this.msg(e)); },
    });
  }

  private upload(id: number, f: File, isEdit: boolean) {
    const data = new FormData();
    data.append('file', f);
    this.http.post<AdminCourse>(`${API}/api/courses/${id}/image`, data).subscribe({
      next: () => this.afterSave(isEdit ? 'Formation et image enregistrées.' : 'Formation ajoutée avec son image.', isEdit),
      error: e => {
        this.saving.set(false);
        this.formError.set('Formation enregistrée, mais l’image a échoué : ' + this.msg(e));
        this.load();
      },
    });
  }

  private afterSave(message: string, isEdit: boolean) {
    this.saving.set(false);
    this.closeForm();
    this.notice.set(message);
    if (!isEdit) this.page.set(0);
    this.load();
  }

  // ------------------------------------------------------------ suppression
  remove(c: AdminCourse) {
    if (c.id == null) return;
    if (!confirm(`Supprimer « ${c.title} » ? Cette action est définitive.`)) return;
    this.http.delete<void>(`${API}/api/courses/${c.id}`).subscribe({
      next: () => {
        this.notice.set('Formation supprimée.');
        if (this.items().length === 1 && this.page() > 0) this.page.update(p => p - 1);
        this.load();
      },
      error: e => this.error.set(this.msg(e)),
    });
  }

  // ----------------------------------------------------------------- import
  importCsv(files: FileList | null) {
    const f = files?.item(0);
    if (!f) return;
    if (!f.name.toLowerCase().endsWith('.csv')) { this.error.set('Choisissez un fichier .csv.'); return; }
    this.importing.set(true);
    const data = new FormData();
    data.append('file', f);
    this.http.post<{ imported: number }>(`${API}/api/courses/import`, data).subscribe({
      next: r => {
        this.importing.set(false);
        this.notice.set(`${r.imported} formations importées.`);
        this.page.set(0);
        this.load();
      },
      error: e => { this.importing.set(false); this.error.set(this.msg(e)); },
    });
  }

  private msg(e: unknown): string {
    const err = e as HttpErrorResponse;
    if (err?.status === 0) return 'Catalogue injoignable. Vérifiez que catalogue-service est lancé (port 8082).';
    if (err?.status === 401) return 'Session expirée : reconnectez-vous.';
    if (err?.status === 403) return 'Action réservée aux administrateurs.';
    if (err?.status === 400) return (err.error?.error as string) ?? 'Données invalides. Vérifiez les champs.';
    return 'Une erreur est survenue. Réessayez.';
  }
}