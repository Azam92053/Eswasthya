import React, { useState, useEffect } from 'react'
import { healthAPI } from '../api'
import { Modal, Button, Input, Select, Textarea } from './ui'

const EMPTY = {
  recordDate: new Date().toISOString().slice(0, 10),
  bmi: '', systolicBp: '', diastolicBp: '',
  glucose: '', activityLevel: '', notes: '',
}

export default function HealthRecordForm({ open, onClose, onSaved, existing }) {
  const [form, setForm]       = useState(EMPTY)
  const [errors, setErrors]   = useState({})
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (open) {
      setErrors({})
      setForm(existing ? {
        recordDate:    existing.recordDate || EMPTY.recordDate,
        bmi:           existing.bmi ?? '',
        systolicBp:    existing.systolicBp ?? '',
        diastolicBp:   existing.diastolicBp ?? '',
        glucose:       existing.glucose ?? '',
        activityLevel: existing.activityLevel || '',
        notes:         existing.notes || '',
      } : { ...EMPTY, recordDate: new Date().toISOString().slice(0, 10) })
    }
  }, [open, existing])

  const set = (k, v) => setForm(f => ({ ...f, [k]: v }))

  const validate = () => {
    const e = {}
    if (!form.recordDate) e.recordDate = 'Date is required'
    if (form.bmi && (form.bmi < 5 || form.bmi > 100)) e.bmi = 'BMI must be 5–100'
    if (form.systolicBp && (form.systolicBp < 50 || form.systolicBp > 300)) e.systolicBp = '50–300 mmHg'
    if (form.diastolicBp && (form.diastolicBp < 30 || form.diastolicBp > 200)) e.diastolicBp = '30–200 mmHg'
    if (form.glucose && (form.glucose < 20 || form.glucose > 800)) e.glucose = '20–800 mg/dL'
    if ((form.systolicBp && !form.diastolicBp) || (!form.systolicBp && form.diastolicBp))
      e.diastolicBp = 'Enter both systolic and diastolic'
    setErrors(e)
    return Object.keys(e).length === 0
  }

  const handleSubmit = async () => {
    if (!validate()) return
    setLoading(true)
    try {
      const payload = {
        recordDate: form.recordDate,
        ...(form.bmi        && { bmi: parseFloat(form.bmi) }),
        ...(form.systolicBp && { systolicBp: parseInt(form.systolicBp) }),
        ...(form.diastolicBp&& { diastolicBp: parseInt(form.diastolicBp) }),
        ...(form.glucose    && { glucose: parseFloat(form.glucose) }),
        ...(form.activityLevel && { activityLevel: form.activityLevel }),
        ...(form.notes      && { notes: form.notes }),
      }
      if (existing) {
        await healthAPI.update(existing.id, payload)
      } else {
        await healthAPI.create(payload)
      }
      onSaved()
      onClose()
    } catch (err) {
      const msg = err.response?.data?.message || 'Failed to save record'
      setErrors({ submit: msg })
    } finally {
      setLoading(false)
    }
  }

  return (
    <Modal open={open} onClose={onClose} title={existing ? 'Edit Health Record' : 'Log Health Metrics'} width={580}>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>

        <Input
          label="Date *"
          type="date"
          value={form.recordDate}
          max={new Date().toISOString().slice(0, 10)}
          onChange={e => set('recordDate', e.target.value)}
          error={errors.recordDate}
        />

        {/* BMI */}
          <div className="glassPanel" style={{ padding: '14px 16px', borderRadius: 'var(--r-lg)', border: '1px solid var(--teal-100)' }}>
          <p style={{ fontSize: 12, fontWeight: 700, color: 'var(--primary)', marginBottom: 10, textTransform: 'uppercase', letterSpacing: .6 }}>
            Body Mass Index
          </p>
          <Input
              label="BMI"
              type="number"
              step="0.1"
              placeholder="e.g. 22.5"
              value={form.bmi}
              onChange={e => set('bmi', e.target.value)}
              error={errors.bmi}
              style={{ background: 'transparent', border: '1.5px solid var(--teal-100)', color: 'var(--text)' }}
            />
          <p style={{ fontSize: 11, color: 'var(--text-light)', marginTop: 6 }}>
            Normal: 18.5–24.9 · Overweight: 25–29.9 · Obese: ≥30
          </p>
        </div>

        {/* Blood Pressure */}
          <div className="glassPanel" style={{ padding: '14px 16px', borderRadius: 'var(--r-lg)', border: '1px solid #f8d5cc' }}>
          <p style={{ fontSize: 12, fontWeight: 700, color: '#c0442a', marginBottom: 10, textTransform: 'uppercase', letterSpacing: .6 }}>
            Blood Pressure (mmHg)
          </p>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
            <Input
              label="Systolic (upper)"
              type="number"
              placeholder="e.g. 120"
              value={form.systolicBp}
              onChange={e => set('systolicBp', e.target.value)}
              error={errors.systolicBp}
              style={{ background: 'transparent', border: '1.5px solid #f8d5cc', color: 'var(--text)' }}
            />
            <Input
              label="Diastolic (lower)"
              type="number"
              placeholder="e.g. 80"
              value={form.diastolicBp}
              onChange={e => set('diastolicBp', e.target.value)}
              error={errors.diastolicBp}
              style={{ background: 'transparent', border: '1.5px solid #f8d5cc', color: 'var(--text)' }}
            />
          </div>
          <p style={{ fontSize: 11, color: '#c0442a', marginTop: 6, opacity: .7 }}>
            Normal: &lt;120/80 · High Stage 1: 130/80 · High Stage 2: ≥140/90
          </p>
        </div>

        {/* Glucose */}
          <div className="glassPanel" style={{ padding: '14px 16px', borderRadius: 'var(--r-lg)', border: '1px solid #f5e8b8' }}>
          <p style={{ fontSize: 12, fontWeight: 700, color: '#9a7010', marginBottom: 10, textTransform: 'uppercase', letterSpacing: .6 }}>
            Fasting Blood Glucose (mg/dL)
          </p>
          <Input
            label="Glucose"
            type="number"
            step="0.1"
            placeholder="e.g. 95"
            value={form.glucose}
            onChange={e => set('glucose', e.target.value)}
            error={errors.glucose}
            style={{ background: 'transparent', border: '1.5px solid #f5e8b8', color: 'var(--text)' }}
          />
          <p style={{ fontSize: 11, color: '#9a7010', marginTop: 6, opacity: .7 }}>
            Normal: 70–99 · Pre-diabetic: 100–125 · Diabetic: ≥126
          </p>
        </div>

        {/* Activity */}
        <Select
          label="Activity Level"
          value={form.activityLevel}
          onChange={e => set('activityLevel', e.target.value)}
          className="on-dark"
        >
          <option value="">— Select activity level —</option>
          <option value="LOW">Low — little or no exercise</option>
          <option value="MODERATE">Moderate — light exercise 1–3 days/week</option>
          <option value="HIGH">High — intense exercise 6–7 days/week</option>
        </Select>

        <Textarea
          label="Notes (optional)"
          placeholder="Any symptoms, medication, or observations…"
          value={form.notes}
          onChange={e => set('notes', e.target.value)}
        />

        {errors.submit && (
          <div style={{ padding: '10px 14px', background: '#fde8ea', borderRadius: 'var(--r-md)', color: 'var(--danger)', fontSize: 13 }}>
            {errors.submit}
          </div>
        )}

        <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end' }}>
          <Button variant="outline" onClick={onClose}>Cancel</Button>
          <Button variant="primary" loading={loading} onClick={handleSubmit}>
            {existing ? 'Save Changes' : 'Log Record'}
          </Button>
        </div>
      </div>
    </Modal>
  )
}
