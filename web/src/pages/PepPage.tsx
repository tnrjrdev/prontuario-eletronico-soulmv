import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Alert, Badge, Button, Card, EmptyState, Skeleton } from '../components/ui'
import { extractError } from '../services/api'
import {
  useAtendimento, usePaciente, useAnamnese, useDiagnosticos,
  useSinaisVitais, useEvolucoes, usePrescricoes, useExames, useRegistrarEvolucao,
} from '../hooks/usePep'
import { useAuth } from '../hooks/useAuth'
import { calcularIdade, formatDate, formatTime } from '../utils/format'
import { SEXO_LABELS, VIA_ADMINISTRACAO_LABELS, STATUS_EXAME_LABELS } from '../utils/constants'
import type { BadgeColor } from '../components/ui'
import type { StatusExame, StatusPrescricao } from '../types'
import {
  ArrowLeft, Activity, FileText, Pill, FlaskConical, AlertTriangle,
  Info, History, MessageSquare, Plus, CheckCircle2, ShieldCheck,
} from 'lucide-react'

type Tab = 'resumo' | 'evolucao' | 'prescricao' | 'exames'

const STATUS_PRESCRICAO_COR: Record<StatusPrescricao, BadgeColor> = {
  ATIVA: 'stable',
  SUSPENSA: 'attention',
  ENCERRADA: 'slate',
}

const STATUS_EXAME_COR: Record<StatusExame, BadgeColor> = {
  SOLICITADO: 'slate',
  COLETADO: 'info',
  EM_ANALISE: 'attention',
  LIBERADO: 'stable',
  CANCELADO: 'critical',
}

export function PepPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const atendimentoId = Number(id)
  const { hasRole } = useAuth()
  const podeEvoluir = hasRole('MEDICO', 'ENFERMEIRO')

  const [activeTab, setActiveTab] = useState<Tab>('resumo')
  const [novaEvolucao, setNovaEvolucao] = useState('')

  const atendimentoQ = useAtendimento(atendimentoId)
  const atendimento = atendimentoQ.data
  const pacienteQ = usePaciente(atendimento?.pacienteId)
  const anamneseQ = useAnamnese(atendimentoId)
  const diagnosticosQ = useDiagnosticos(atendimentoId)
  const sinaisQ = useSinaisVitais(atendimentoId)
  const evolucoesQ = useEvolucoes(atendimentoId)
  const prescricoesQ = usePrescricoes(atendimentoId)
  const examesQ = useExames(atendimentoId)
  const registrarEvolucao = useRegistrarEvolucao(atendimentoId)

  // --- Estados de carregamento / erro do recurso principal ---
  if (atendimentoQ.isLoading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-20 w-full" />
        <div className="flex gap-4">
          <Skeleton className="h-96 w-80 hidden lg:block" />
          <Skeleton className="h-96 flex-1" />
        </div>
      </div>
    )
  }
  if (atendimentoQ.isError || !atendimento) {
    return (
      <div className="p-8">
        <Alert variant="destructive">
          {atendimentoQ.error ? extractError(atendimentoQ.error) : 'Atendimento não encontrado.'}
        </Alert>
        <Button className="mt-4" onClick={() => navigate('/atendimentos')}>Voltar para a fila</Button>
      </div>
    )
  }

  // --- Dados derivados (já reais) ---
  const idade = calcularIdade(pacienteQ.data?.dataNascimento)
  const sexo = pacienteQ.data?.sexo
  const alergias = anamneseQ.data?.alergias?.trim()
  const diagnosticos = diagnosticosQ.data ?? []
  const sinais = [...(sinaisQ.data ?? [])].sort((a, b) => b.dataHora.localeCompare(a.dataHora))
  const ultimoSinal = sinais[0]
  const evolucoes = [...(evolucoesQ.data ?? [])].sort((a, b) => b.dataHora.localeCompare(a.dataHora))
  const ultimaEvolucao = evolucoes[0]
  const prescricoes = prescricoesQ.data ?? []
  const itensAtivos = prescricoes.filter((p) => p.status === 'ATIVA').flatMap((p) => p.itens)
  const exames = examesQ.data ?? []

  const salvarEvolucao = async () => {
    if (!novaEvolucao.trim()) return
    try {
      await registrarEvolucao.mutateAsync(novaEvolucao.trim())
      setNovaEvolucao('')
    } catch {
      /* o erro é exibido inline abaixo via registrarEvolucao.isError */
    }
  }

  return (
    <div className="flex flex-col h-[calc(100vh-8rem)] -m-8">
      {/* Banner do paciente */}
      <div className="bg-card border-b border-border p-4 flex items-center justify-between shrink-0 shadow-sm z-10">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={() => navigate('/atendimentos')} aria-label="Voltar para a fila" className="text-muted-foreground hover:text-foreground">
            <ArrowLeft className="h-5 w-5" />
          </Button>
          <div className="flex flex-col">
            <div className="flex items-center gap-3">
              <h1 className="text-2xl font-bold tracking-tight text-foreground">{atendimento.pacienteNome}</h1>
              <Badge color="blue" className="text-xs">ID: {String(atendimento.pacienteId).padStart(6, '0')}</Badge>
              <Badge color="slate" className="text-xs">Atend: #{String(atendimento.id).padStart(4, '0')}</Badge>
            </div>
            <div className="flex flex-wrap gap-x-4 gap-y-1 text-sm text-muted-foreground mt-1 font-medium">
              <span>{atendimento.tipo}</span>
              <span>•</span>
              <span>{atendimento.setorNome}</span>
              {atendimento.leitoIdentificador && (<><span>•</span><span>Leito {atendimento.leitoIdentificador}</span></>)}
              <span>•</span>
              {anamneseQ.isLoading ? (
                <span className="text-muted-foreground">verificando alergias…</span>
              ) : alergias ? (
                <span className="flex items-center gap-1 text-rose-600 dark:text-rose-500 font-semibold">
                  <AlertTriangle className="h-3.5 w-3.5" /> Alergia: {alergias}
                </span>
              ) : (
                <span className="flex items-center gap-1 text-emerald-600">
                  <ShieldCheck className="h-3.5 w-3.5" /> Sem alergias registradas
                </span>
              )}
            </div>
          </div>
        </div>
        <div className="flex items-center gap-3">
          <Badge color="info">{atendimento.status}</Badge>
        </div>
      </div>

      {/* Grid de 3 colunas */}
      <div className="flex flex-1 overflow-hidden bg-slate-50/50">
        {/* Esquerda - Contexto clínico */}
        <aside className="w-80 border-r border-border bg-card overflow-y-auto hidden lg:block shrink-0">
          <div className="p-4 space-y-6">
            <section>
              <h3 className="text-sm font-semibold uppercase tracking-wider text-muted-foreground mb-3 flex items-center gap-2"><Info className="h-4 w-4" /> Dados Básicos</h3>
              {pacienteQ.isLoading ? (
                <div className="space-y-2"><Skeleton className="h-5 w-full" /><Skeleton className="h-5 w-2/3" /></div>
              ) : (
                <div className="space-y-2 text-sm">
                  <div className="flex justify-between"><span className="text-muted-foreground">Idade:</span><span className="font-medium">{idade != null ? `${idade} anos` : '—'}</span></div>
                  <div className="flex justify-between"><span className="text-muted-foreground">Sexo:</span><span className="font-medium">{sexo ? SEXO_LABELS[sexo] : '—'}</span></div>
                  {pacienteQ.data?.convenioNome && (
                    <div className="flex justify-between"><span className="text-muted-foreground">Convênio:</span><span className="font-medium">{pacienteQ.data.convenioNome}</span></div>
                  )}
                </div>
              )}
            </section>

            <section className="border-t border-border pt-4">
              <h3 className="text-sm font-semibold uppercase tracking-wider text-muted-foreground mb-3 flex items-center gap-2"><AlertTriangle className="h-4 w-4" /> Riscos / Alergias</h3>
              {anamneseQ.isLoading ? (
                <Skeleton className="h-6 w-32" />
              ) : alergias ? (
                <Badge variant="outline" className="border-rose-200 bg-rose-50 text-rose-700">Alergia: {alergias}</Badge>
              ) : (
                <p className="text-sm text-muted-foreground">Nenhum risco registrado na anamnese.</p>
              )}
            </section>

            <section className="border-t border-border pt-4">
              <h3 className="text-sm font-semibold uppercase tracking-wider text-muted-foreground mb-3 flex items-center gap-2"><History className="h-4 w-4" /> Diagnósticos</h3>
              {diagnosticosQ.isLoading ? (
                <div className="space-y-2"><Skeleton className="h-5 w-full" /><Skeleton className="h-5 w-3/4" /></div>
              ) : diagnosticos.length === 0 ? (
                <p className="text-sm text-muted-foreground">Nenhum diagnóstico registrado.</p>
              ) : (
                <ul className="space-y-2 text-sm">
                  {diagnosticos.map((d) => (
                    <li key={d.id} className="flex items-start gap-2">
                      <span className="text-primary font-mono text-xs mt-0.5">{d.cid10Codigo}</span>
                      <span className="font-medium text-foreground">{d.cid10Descricao}{d.tipo === 'PRINCIPAL' && <span className="ml-1 text-xs text-muted-foreground">(principal)</span>}</span>
                    </li>
                  ))}
                </ul>
              )}
            </section>
          </div>
        </aside>

        {/* Centro - Abas */}
        <div className="flex-1 flex flex-col overflow-hidden bg-background">
          <div role="tablist" aria-label="Seções do prontuário" className="flex border-b border-border bg-card px-2 shrink-0">
            {([
              ['resumo', 'Resumo Clínico', Activity],
              ['evolucao', 'Evoluções', FileText],
              ['prescricao', 'Prescrições', Pill],
              ['exames', 'Exames', FlaskConical],
            ] as const).map(([tab, label, Icon]) => (
              <button
                key={tab}
                role="tab"
                aria-selected={activeTab === tab}
                onClick={() => setActiveTab(tab)}
                className={`px-4 py-3 text-sm font-medium border-b-2 transition-colors flex items-center gap-2 ${activeTab === tab ? 'border-primary text-primary' : 'border-transparent text-muted-foreground hover:text-foreground'}`}
              >
                <Icon className="h-4 w-4" /> {label}
              </button>
            ))}
          </div>

          <div className="flex-1 overflow-y-auto p-6">
            {/* RESUMO */}
            {activeTab === 'resumo' && (
              <div className="space-y-6 max-w-4xl mx-auto">
                <Card title="Última Evolução">
                  {evolucoesQ.isLoading ? (
                    <div className="space-y-2"><Skeleton className="h-4 w-full" /><Skeleton className="h-4 w-5/6" /></div>
                  ) : !ultimaEvolucao ? (
                    <p className="text-sm text-muted-foreground">Nenhuma evolução registrada neste atendimento.</p>
                  ) : (
                    <>
                      <p className="text-sm text-foreground leading-relaxed whitespace-pre-wrap">{ultimaEvolucao.texto}</p>
                      <div className="mt-4 pt-4 border-t border-border flex items-center justify-between text-xs text-muted-foreground">
                        <span>Por: {ultimaEvolucao.autorNome} ({ultimaEvolucao.tipo === 'MEDICA' ? 'Médica' : 'Enfermagem'})</span>
                        <span>{formatDate(ultimaEvolucao.dataHora)}</span>
                      </div>
                    </>
                  )}
                </Card>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <Card title="Sinais Vitais Recentes">
                    {sinaisQ.isLoading ? (
                      <Skeleton className="h-24 w-full" />
                    ) : !ultimoSinal ? (
                      <p className="text-sm text-muted-foreground">Sem aferições registradas.</p>
                    ) : (
                      <>
                        <div className="grid grid-cols-2 gap-4 text-sm">
                          <Vital label="Pressão Arterial" valor={ultimoSinal.pressaoSistolica && ultimoSinal.pressaoDiastolica ? `${ultimoSinal.pressaoSistolica}x${ultimoSinal.pressaoDiastolica}` : '—'} unidade="mmHg" />
                          <Vital label="Freq. Cardíaca" valor={ultimoSinal.frequenciaCardiaca ?? '—'} unidade="bpm" />
                          <Vital label="Saturação O₂" valor={ultimoSinal.saturacaoO2 ?? '—'} unidade="%" />
                          <Vital label="Temperatura" valor={ultimoSinal.temperatura ?? '—'} unidade="°C" />
                        </div>
                        <p className="mt-3 text-xs text-muted-foreground">Aferido em {formatDate(ultimoSinal.dataHora)}</p>
                      </>
                    )}
                  </Card>

                  <Card title="Medicamentos Ativos">
                    {prescricoesQ.isLoading ? (
                      <Skeleton className="h-24 w-full" />
                    ) : itensAtivos.length === 0 ? (
                      <p className="text-sm text-muted-foreground">Nenhuma prescrição ativa.</p>
                    ) : (
                      <ul className="space-y-3 text-sm">
                        {itensAtivos.map((item) => (
                          <li key={item.id} className="flex items-start justify-between pb-2 border-b border-border last:border-0">
                            <div className="font-medium text-foreground">
                              {item.medicamentoNome} {item.dose} {VIA_ADMINISTRACAO_LABELS[item.via]}
                              {item.medicamentoControlado && <Badge color="critical" className="ml-2 text-[10px]">Controlado</Badge>}
                            </div>
                            <div className="text-muted-foreground whitespace-nowrap pl-2">{item.frequencia}</div>
                          </li>
                        ))}
                      </ul>
                    )}
                  </Card>
                </div>
              </div>
            )}

            {/* EVOLUÇÕES */}
            {activeTab === 'evolucao' && (
              <div className="max-w-4xl mx-auto space-y-6">
                {podeEvoluir && (
                  <Card className="border-primary bg-primary/5">
                    <div className="space-y-3">
                      <h3 className="font-semibold text-primary flex items-center gap-2"><MessageSquare className="h-4 w-4" /> Nova Evolução Clínica</h3>
                      <textarea
                        className="w-full min-h-[140px] p-3 text-sm rounded-md border border-input bg-background focus:ring-2 focus:ring-primary focus:outline-none resize-y"
                        placeholder="Digite a evolução do paciente…"
                        value={novaEvolucao}
                        onChange={(e) => setNovaEvolucao(e.target.value)}
                      />
                      {registrarEvolucao.isError && (
                        <Alert variant="destructive">{extractError(registrarEvolucao.error)}</Alert>
                      )}
                      <div className="flex justify-end">
                        <Button onClick={salvarEvolucao} disabled={registrarEvolucao.isPending || !novaEvolucao.trim()}>
                          <CheckCircle2 className="h-4 w-4 mr-2" />
                          {registrarEvolucao.isPending ? 'Salvando…' : 'Assinar e Salvar'}
                        </Button>
                      </div>
                    </div>
                  </Card>
                )}

                {evolucoesQ.isLoading ? (
                  <div className="space-y-3"><Skeleton className="h-20 w-full" /><Skeleton className="h-20 w-full" /></div>
                ) : evolucoes.length === 0 ? (
                  <EmptyState icon={<FileText />} title="Sem evoluções" description="Nenhuma evolução foi registrada neste atendimento ainda." />
                ) : (
                  <div className="space-y-3">
                    {evolucoes.map((evo) => (
                      <div key={evo.id} className="p-4 rounded-lg border border-border bg-card shadow-sm">
                        <div className="flex items-center justify-between mb-1">
                          <span className="font-semibold text-foreground">{evo.autorNome}
                            <Badge color={evo.tipo === 'MEDICA' ? 'info' : 'stable'} className="ml-2 text-[10px]">{evo.tipo === 'MEDICA' ? 'Médica' : 'Enfermagem'}</Badge>
                          </span>
                          <span className="text-xs text-muted-foreground">{formatDate(evo.dataHora)}</span>
                        </div>
                        <p className="text-sm text-foreground whitespace-pre-wrap leading-relaxed">{evo.texto}</p>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {/* PRESCRIÇÕES */}
            {activeTab === 'prescricao' && (
              <div className="max-w-5xl mx-auto space-y-6">
                {prescricoesQ.isLoading ? (
                  <Skeleton className="h-48 w-full" />
                ) : prescricoes.length === 0 ? (
                  <EmptyState icon={<Pill />} title="Sem prescrições" description="Nenhuma prescrição registrada para este atendimento." />
                ) : (
                  prescricoes.map((presc) => (
                    <Card key={presc.id} className="p-0 overflow-hidden shadow-sm">
                      <div className="flex items-center justify-between px-4 py-3 border-b border-border bg-muted/30">
                        <div className="text-sm">
                          <span className="font-semibold text-foreground">Prescrição #{presc.id}</span>
                          <span className="text-muted-foreground ml-2">{presc.medicoNome} • {formatDate(presc.dataHora)}</span>
                        </div>
                        <Badge color={STATUS_PRESCRICAO_COR[presc.status]}>{presc.status}</Badge>
                      </div>
                      <table className="w-full text-sm">
                        <thead className="bg-muted/40 border-b border-border">
                          <tr className="text-left text-muted-foreground font-medium uppercase text-xs tracking-wider">
                            <th className="px-4 py-2">Medicamento</th>
                            <th className="px-4 py-2">Dose</th>
                            <th className="px-4 py-2">Via</th>
                            <th className="px-4 py-2">Frequência</th>
                            <th className="px-4 py-2">Duração</th>
                          </tr>
                        </thead>
                        <tbody className="divide-y divide-border">
                          {presc.itens.map((item) => (
                            <tr key={item.id} className="hover:bg-muted/20">
                              <td className="px-4 py-2 font-medium text-foreground">
                                {item.medicamentoNome}
                                {item.medicamentoControlado && <Badge color="critical" className="ml-2 text-[10px]">Controlado</Badge>}
                              </td>
                              <td className="px-4 py-2">{item.dose}</td>
                              <td className="px-4 py-2">{VIA_ADMINISTRACAO_LABELS[item.via]}</td>
                              <td className="px-4 py-2">{item.frequencia}</td>
                              <td className="px-4 py-2 text-muted-foreground">{item.duracao || '—'}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </Card>
                  ))
                )}
              </div>
            )}

            {/* EXAMES */}
            {activeTab === 'exames' && (
              <div className="max-w-4xl mx-auto space-y-3">
                {examesQ.isLoading ? (
                  <Skeleton className="h-40 w-full" />
                ) : exames.length === 0 ? (
                  <EmptyState
                    icon={<FlaskConical />}
                    title="Sem exames"
                    description="Nenhum exame solicitado neste atendimento."
                    action={<Button variant="outline"><Plus className="mr-2 h-4 w-4" /> Solicitar Exames</Button>}
                  />
                ) : (
                  exames.map((ex) => (
                    <Card key={ex.id} className="shadow-sm">
                      <div className="flex items-start justify-between">
                        <div>
                          <div className="font-semibold text-foreground">{ex.tipoExame}</div>
                          <div className="text-xs text-muted-foreground mt-1">
                            Solicitado por {ex.medicoSolicitanteNome ?? '—'} • {formatDate(ex.dataSolicitacao)}
                          </div>
                          {ex.resultado?.resultadoTexto && (
                            <p className="text-sm text-foreground mt-3 whitespace-pre-wrap">{ex.resultado.resultadoTexto}</p>
                          )}
                        </div>
                        <Badge color={STATUS_EXAME_COR[ex.status]}>{STATUS_EXAME_LABELS[ex.status]}</Badge>
                      </div>
                    </Card>
                  ))
                )}
              </div>
            )}
          </div>
        </div>

        {/* Direita - Resumo rápido de vitais */}
        <aside className="w-64 border-l border-border bg-card hidden xl:block shrink-0 overflow-y-auto">
          <div className="p-4 border-b border-border bg-muted/20">
            <h3 className="text-xs font-semibold uppercase tracking-wider text-muted-foreground">Monitorização</h3>
          </div>
          <div className="p-4 space-y-3 text-sm">
            {ultimoSinal ? (
              <>
                <div className="flex justify-between"><span className="text-muted-foreground">PA</span><span className="font-mono font-semibold">{ultimoSinal.pressaoSistolica}x{ultimoSinal.pressaoDiastolica}</span></div>
                <div className="flex justify-between"><span className="text-muted-foreground">FC</span><span className="font-mono font-semibold">{ultimoSinal.frequenciaCardiaca ?? '—'}</span></div>
                <div className="flex justify-between"><span className="text-muted-foreground">SpO₂</span><span className="font-mono font-semibold">{ultimoSinal.saturacaoO2 ?? '—'}%</span></div>
                <div className="flex justify-between"><span className="text-muted-foreground">Temp</span><span className="font-mono font-semibold">{ultimoSinal.temperatura ?? '—'}°C</span></div>
                {ultimoSinal.escalaDor != null && (
                  <div className="flex justify-between"><span className="text-muted-foreground">Dor</span><span className="font-mono font-semibold">{ultimoSinal.escalaDor}/10</span></div>
                )}
                <p className="pt-2 text-xs text-muted-foreground">Última às {formatTime(ultimoSinal.dataHora)}</p>
              </>
            ) : (
              <p className="text-muted-foreground">Sem sinais vitais.</p>
            )}
          </div>
        </aside>
      </div>
    </div>
  )
}

function Vital({ label, valor, unidade }: { label: string; valor: React.ReactNode; unidade: string }) {
  return (
    <div className="bg-muted/30 p-3 rounded-md border border-border">
      <div className="text-muted-foreground mb-1 text-xs uppercase font-semibold">{label}</div>
      <div className="font-mono text-lg font-bold">{valor} <span className="text-xs font-sans font-normal text-muted-foreground">{unidade}</span></div>
    </div>
  )
}
