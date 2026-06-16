import { useEffect, useState, type FormEvent } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Alert, Badge, Button, Card } from '../components/ui'
import { atendimentoService } from '../services/atendimento.service'
import { clinicoService } from '../services/clinico.service'
import { triagemService, type SinaisVitaisPayload } from '../services/triagem.service'
import { extractError } from '../services/api'
import type { Atendimento, ClassificacaoRisco } from '../types'
import { ArrowLeft, Activity, Heart, Thermometer, Droplet, Wind, AlertTriangle, ShieldCheck, CheckCircle2 } from 'lucide-react'

// Protocolo de Manchester — ids batem com o enum ClassificacaoRisco do backend.
const NIVEIS_RISCO: { id: ClassificacaoRisco; label: string; color: string }[] = [
  { id: 'VERMELHO', label: 'Emergência (0 min)', color: 'bg-rose-600 hover:bg-rose-700 text-white border-rose-700' },
  { id: 'LARANJA', label: 'Muito Urgente (10 min)', color: 'bg-orange-500 hover:bg-orange-600 text-white border-orange-600' },
  { id: 'AMARELO', label: 'Urgente (60 min)', color: 'bg-amber-400 hover:bg-amber-500 text-amber-950 border-amber-500' },
  { id: 'VERDE', label: 'Pouco Urgente (120 min)', color: 'bg-emerald-500 hover:bg-emerald-600 text-white border-emerald-600' },
  { id: 'AZUL', label: 'Não Urgente (240 min)', color: 'bg-blue-500 hover:bg-blue-600 text-white border-blue-600' },
]

export function TriagemPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const atendimentoId = Number(id)
  const [atendimento, setAtendimento] = useState<Atendimento | null>(null)
  const [alergias, setAlergias] = useState<string | null>(null)
  const [alergiasCarregando, setAlergiasCarregando] = useState(true)
  const [erro, setErro] = useState('')
  const [salvando, setSalvando] = useState(false)

  const [form, setForm] = useState({
    pressaoSistolica: '',
    pressaoDiastolica: '',
    frequenciaCardiaca: '',
    temperatura: '',
    saturacaoO2: '',
    glicemia: '',
    escalaDor: '',
    queixaPrincipal: '',
    classificacaoRisco: '' as '' | ClassificacaoRisco,
  })

  useEffect(() => {
    if (!atendimentoId) return
    atendimentoService.buscar(atendimentoId)
      .then(setAtendimento)
      .catch((err) => setErro(extractError(err)))
    clinicoService.anamnese(atendimentoId)
      .then((a) => setAlergias(a?.alergias?.trim() || null))
      .finally(() => setAlergiasCarregando(false))
  }, [atendimentoId])

  const num = (v: string) => (v.trim() === '' ? undefined : Number(v))

  const salvarTriagem = async (e: FormEvent) => {
    e.preventDefault()
    if (!form.classificacaoRisco) {
      setErro('A Classificação de Risco é obrigatória.')
      return
    }
    setErro('')
    setSalvando(true)
    try {
      const vitais: SinaisVitaisPayload = {
        pressaoSistolica: num(form.pressaoSistolica),
        pressaoDiastolica: num(form.pressaoDiastolica),
        frequenciaCardiaca: num(form.frequenciaCardiaca),
        temperatura: num(form.temperatura),
        saturacaoO2: num(form.saturacaoO2),
        glicemia: num(form.glicemia),
        escalaDor: num(form.escalaDor),
      }
      if (Object.values(vitais).some((v) => v !== undefined)) {
        await triagemService.registrarSinaisVitais(atendimentoId, vitais)
      }
      // Registra a triagem (o backend já transiciona o status para AGUARDANDO_ATENDIMENTO).
      await triagemService.registrar(atendimentoId, {
        classificacaoRisco: form.classificacaoRisco,
        observacao: form.queixaPrincipal || undefined,
      })
      navigate('/atendimentos')
    } catch (err) {
      setErro(extractError(err))
    } finally {
      setSalvando(false)
    }
  }

  if (erro && !atendimento) return <div className="p-8"><Alert variant="destructive">{erro}</Alert><Button className="mt-4" onClick={() => navigate('/atendimentos')}>Voltar</Button></div>
  if (!atendimento) return <div className="p-8 text-center text-muted-foreground">Carregando Triagem...</div>

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      {/* Header Triagem */}
      <div className="flex items-center gap-4 border-b border-border pb-4">
        <Button variant="ghost" size="icon" onClick={() => navigate('/atendimentos')} className="text-muted-foreground hover:text-foreground shrink-0">
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-bold tracking-tight text-foreground">Triagem: {atendimento.pacienteNome}</h1>
            <Badge color="slate">ID: {String(atendimento.pacienteId).padStart(6, '0')}</Badge>
          </div>
          <p className="text-muted-foreground text-sm flex flex-wrap items-center gap-2 mt-1">
            <span>Atendimento: #{String(atendimento.id).padStart(4, '0')}</span>
            <span>•</span>
            <span>{atendimento.tipo}</span>
            <span>•</span>
            {alergiasCarregando ? (
              <span className="text-muted-foreground">verificando alergias…</span>
            ) : alergias ? (
              <span className="text-rose-600 flex items-center gap-1 font-semibold"><AlertTriangle className="h-3 w-3" /> Alergia: {alergias}</span>
            ) : (
              <span className="text-emerald-600 flex items-center gap-1"><ShieldCheck className="h-3 w-3" /> Sem alergias registradas</span>
            )}
          </p>
        </div>
      </div>

      {erro && <Alert variant="destructive">{erro}</Alert>}

      <form onSubmit={salvarTriagem} className="space-y-8">
        {/* Motivo e Anamnese Breve */}
        <Card title="Queixa Principal e Histórico Breve" className="border-l-4 border-l-primary">
          <div className="p-4 pt-0">
            <textarea
              className="w-full min-h-[100px] p-3 text-sm rounded-md border border-input bg-background focus:ring-2 focus:ring-primary focus:outline-none resize-y"
              placeholder="Descreva o motivo da busca por atendimento (ex: 'Paciente relata dor torácica irradiando para o braço esquerdo há 2 horas...')"
              value={form.queixaPrincipal}
              onChange={(e) => setForm({ ...form, queixaPrincipal: e.target.value })}
              required
            />
          </div>
        </Card>

        {/* Coleta de Sinais Vitais */}
        <div>
          <h3 className="text-lg font-semibold text-foreground mb-4 flex items-center gap-2"><Activity className="h-5 w-5 text-primary" /> Sinais Vitais</h3>
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-4">
            <div className="col-span-2 md:col-span-1 border border-border rounded-lg bg-card p-3 shadow-sm flex flex-col justify-between">
              <div className="text-xs font-semibold uppercase text-muted-foreground mb-2 flex items-center gap-1"><Heart className="h-3 w-3" /> P.A. (mmHg)</div>
              <div className="flex items-center gap-2">
                <input type="number" className="w-16 h-10 text-center font-mono text-lg rounded border border-input bg-background focus:ring-2 focus:ring-primary focus:outline-none" placeholder="120" value={form.pressaoSistolica} onChange={e => setForm({ ...form, pressaoSistolica: e.target.value })} />
                <span className="text-muted-foreground font-light text-xl">/</span>
                <input type="number" className="w-16 h-10 text-center font-mono text-lg rounded border border-input bg-background focus:ring-2 focus:ring-primary focus:outline-none" placeholder="80" value={form.pressaoDiastolica} onChange={e => setForm({ ...form, pressaoDiastolica: e.target.value })} />
              </div>
            </div>

            <div className="border border-border rounded-lg bg-card p-3 shadow-sm flex flex-col justify-between">
              <div className="text-xs font-semibold uppercase text-muted-foreground mb-2 flex items-center gap-1"><Activity className="h-3 w-3" /> F.C. (bpm)</div>
              <input type="number" className="w-full h-10 px-2 font-mono text-lg rounded border border-input bg-background focus:ring-2 focus:ring-primary focus:outline-none" placeholder="72" value={form.frequenciaCardiaca} onChange={e => setForm({ ...form, frequenciaCardiaca: e.target.value })} />
            </div>

            <div className="border border-border rounded-lg bg-card p-3 shadow-sm flex flex-col justify-between">
              <div className="text-xs font-semibold uppercase text-muted-foreground mb-2 flex items-center gap-1"><Thermometer className="h-3 w-3" /> Temp. (°C)</div>
              <input type="number" step="0.1" className="w-full h-10 px-2 font-mono text-lg rounded border border-input bg-background focus:ring-2 focus:ring-primary focus:outline-none" placeholder="36.5" value={form.temperatura} onChange={e => setForm({ ...form, temperatura: e.target.value })} />
            </div>

            <div className="border border-border rounded-lg bg-card p-3 shadow-sm flex flex-col justify-between">
              <div className="text-xs font-semibold uppercase text-muted-foreground mb-2 flex items-center gap-1"><Wind className="h-3 w-3" /> SpO2 (%)</div>
              <input type="number" className="w-full h-10 px-2 font-mono text-lg rounded border border-input bg-background focus:ring-2 focus:ring-primary focus:outline-none" placeholder="98" value={form.saturacaoO2} onChange={e => setForm({ ...form, saturacaoO2: e.target.value })} />
            </div>

            <div className="border border-border rounded-lg bg-card p-3 shadow-sm flex flex-col justify-between">
              <div className="text-xs font-semibold uppercase text-muted-foreground mb-2 flex items-center gap-1"><Droplet className="h-3 w-3" /> Glic. (mg/dL)</div>
              <input type="number" className="w-full h-10 px-2 font-mono text-lg rounded border border-input bg-background focus:ring-2 focus:ring-primary focus:outline-none" placeholder="90" value={form.glicemia} onChange={e => setForm({ ...form, glicemia: e.target.value })} />
            </div>

            <div className="border border-border rounded-lg bg-card p-3 shadow-sm flex flex-col justify-between">
              <div className="text-xs font-semibold uppercase text-muted-foreground mb-2 flex items-center gap-1">Dor (0-10)</div>
              <input type="number" min={0} max={10} className="w-full h-10 px-2 font-mono text-lg rounded border border-input bg-background focus:ring-2 focus:ring-primary focus:outline-none" placeholder="0" value={form.escalaDor} onChange={e => setForm({ ...form, escalaDor: e.target.value })} />
            </div>
          </div>
        </div>

        {/* Protocolo de Manchester */}
        <div>
          <h3 className="text-lg font-semibold text-foreground mb-4">Classificação de Risco</h3>
          <div className="grid grid-cols-1 md:grid-cols-5 gap-3">
            {NIVEIS_RISCO.map(nivel => (
              <button
                key={nivel.id}
                type="button"
                onClick={() => setForm({ ...form, classificacaoRisco: nivel.id })}
                className={`p-4 rounded-lg border-2 text-center transition-all duration-200 shadow-sm flex flex-col items-center justify-center gap-2 ${
                  form.classificacaoRisco === nivel.id
                    ? `ring-4 ring-offset-2 ring-primary ${nivel.color} scale-105 z-10`
                    : `${nivel.color} opacity-70 hover:opacity-100`
                }`}
              >
                <span className="font-bold">{nivel.label}</span>
              </button>
            ))}
          </div>
        </div>

        {/* Footer Actions */}
        <div className="flex justify-end pt-6 border-t border-border">
          <Button type="button" variant="ghost" className="mr-4" onClick={() => navigate('/atendimentos')}>Cancelar</Button>
          <Button type="submit" disabled={salvando} size="lg" className="bg-primary hover:bg-primary/90">
            <CheckCircle2 className="mr-2 h-5 w-5" />
            {salvando ? 'Salvando...' : 'Finalizar Triagem'}
          </Button>
        </div>
      </form>
    </div>
  )
}
