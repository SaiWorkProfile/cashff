import React, { useCallback, useEffect, useState } from 'react';
import { api, uploadUrl } from './api';

const SLOTS = ['9:00 AM', '5:00 PM', '9:00 PM'];
const EMPTY_HOME = { donorSlots: { '9:00 AM': [], '5:00 PM': [], '9:00 PM': [] }, bannerUrl: '' };
const fileUrl = (path) => uploadUrl(path);
const money = (value) => `₹${Number(value || 0).toLocaleString('en-IN')}`;
const formatCountdown = (seconds) => {
  const total = Math.max(0, seconds);
  const hours = Math.floor(total / 3600);
  const minutes = Math.floor((total % 3600) / 60);
  const remainder = total % 60;
  return hours
    ? `${hours}:${String(minutes).padStart(2, '0')}:${String(remainder).padStart(2, '0')}`
    : `${String(minutes).padStart(2, '0')}:${String(remainder).padStart(2, '0')}`;
};

export default function App() {
  const [isAdmin, setIsAdmin] = useState(Boolean(localStorage.getItem('cashff_token')));
  const [username, setUsername] = useState(localStorage.getItem('cashff_username') || '');
  const [view, setView] = useState('home');
  const [home, setHome] = useState(EMPTY_HOME);
  const [allDonorSlots, setAllDonorSlots] = useState(EMPTY_HOME.donorSlots);
  const [codes, setCodes] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [qrUrl, setQrUrl] = useState('');
  const [loginOpen, setLoginOpen] = useState(false);
  const [loginBusy, setLoginBusy] = useState(false);
  const [loginError, setLoginError] = useState('');
  const [loginForm, setLoginForm] = useState({ username: '', password: '' });
  const [activeSlot, setActiveSlot] = useState(SLOTS[0]);
  const [noticeOpen, setNoticeOpen] = useState(false);
  const [donorModal, setDonorModal] = useState(false);
  const [donorSlot, setDonorSlot] = useState(SLOTS[0]);
  const [donorDraft, setDonorDraft] = useState([]);
  const [codesText, setCodesText] = useState('');
  const [codeStartTime, setCodeStartTime] = useState('');
  const [codeEndTime, setCodeEndTime] = useState('');
  const [roomDraft, setRoomDraft] = useState({ roomCode: '', passwordCode: '111', startAt: '', endAt: '' });
  const [legacyRoomToSchedule, setLegacyRoomToSchedule] = useState(null);
  const [winnerRoom, setWinnerRoom] = useState(null);
  const [winnerFiles, setWinnerFiles] = useState([null, null, null]);
  const [photosToView, setPhotosToView] = useState(null);
  const [imageViewer, setImageViewer] = useState(null);
  const [bannerFile, setBannerFile] = useState(null);
  const [bannerStartTime, setBannerStartTime] = useState('');
  const [bannerEndTime, setBannerEndTime] = useState('');
  const [scheduledBanners, setScheduledBanners] = useState([]);
  const [qrFile, setQrFile] = useState(null);
  const [donationForm, setDonationForm] = useState({ whatsapp: '', amount: '', screenshot: null, agree: false });
  const [donationBusy, setDonationBusy] = useState(false);
  const [dashboardMode, setDashboardMode] = useState(false);
  const [donations, setDonations] = useState([]);
  const [donationFilters, setDonationFilters] = useState({ startTime: '', endTime: '', sortBy: 'latest' });
  const [notifyCodes, setNotifyCodes] = useState(localStorage.getItem('cashff_notify_codes') !== 'false');
  const [notifyRooms, setNotifyRooms] = useState(localStorage.getItem('cashff_notify_rooms') !== 'false');
  const [tick, setTick] = useState(() => performance.now());
  const [roomsReceivedAt, setRoomsReceivedAt] = useState(() => performance.now());
  const [busy, setBusy] = useState(false);
  const [flash, setFlash] = useState('');

  const tell = useCallback((message) => {
    setFlash(message);
    window.clearTimeout(tell.timer);
    tell.timer = window.setTimeout(() => setFlash(''), 4200);
  }, []);

  const loadHome = useCallback(async () => {
    const data = await api('/api/public/home');
    const donorSlots = data?.donorSlots || EMPTY_HOME.donorSlots;
    setActiveSlot(data?.activeSlot || SLOTS[0]);
    setHome({ donorSlots, bannerUrl: data?.bannerUrl || '', activeSlot: data?.activeSlot || null });
  }, []);
  const loadAdminDonorSlots = useCallback(async () => {
    const data = await api('/api/admin/donor-slots');
    setAllDonorSlots(data || EMPTY_HOME.donorSlots);
    return data || EMPTY_HOME.donorSlots;
  }, []);
  const loadCodes = useCallback(async () => setCodes(await api('/api/public/redeem-codes')) , []);
  const loadRooms = useCallback(async () => {
    const items = await api(isAdmin ? '/api/admin/rooms' : '/api/public/rooms');
    setRoomsReceivedAt(performance.now());
    setRooms(items || []);
    return items || [];
  }, [isAdmin]);
  const loadScheduledBanners = useCallback(async () => {
    const items = await api('/api/admin/banners');
    setScheduledBanners(items || []);
    return items || [];
  }, []);
  const loadQr = useCallback(async () => { const data = await api('/api/public/support/qr'); setQrUrl(data?.qrUrl || ''); }, []);
  const refreshPublic = useCallback(async () => {
    try { await Promise.all([loadHome(), loadCodes(), loadRooms(), loadQr()]); } catch (e) { tell(`API connection: ${e.message}. Start the Spring Boot server and check the database.`); }
  }, [loadHome, loadCodes, loadRooms, loadQr, tell]);

  useEffect(() => { refreshPublic(); }, [refreshPublic]);
  useEffect(() => {
    const refreshVisible = () => {
      if (document.visibilityState === 'visible') {
        loadHome().catch((e) => tell(`Home refresh failed: ${e.message}`));
        loadRooms().catch((e) => tell(`Room refresh failed: ${e.message}`));
      }
    };
    const timer = window.setInterval(refreshVisible, 30000);
    document.addEventListener('visibilitychange', refreshVisible);
    window.addEventListener('focus', refreshVisible);
    return () => {
      window.clearInterval(timer);
      document.removeEventListener('visibilitychange', refreshVisible);
      window.removeEventListener('focus', refreshVisible);
    };
  }, [loadHome, loadRooms, tell]);
  useEffect(() => {
    if (view !== 'g_rcode') return undefined;
    const refreshCodes = () => loadCodes().catch((e) => tell(`Code refresh failed: ${e.message}`));
    refreshCodes();
    const timer = window.setInterval(refreshCodes, 5000);
    return () => window.clearInterval(timer);
  }, [view, loadCodes, tell]);
  useEffect(() => {
    if (view !== 'custom') return undefined;
    const refreshRooms = () => loadRooms().catch((e) => tell(`Room refresh failed: ${e.message}`));
    refreshRooms();
    const timer = window.setInterval(refreshRooms, 1000);
    return () => window.clearInterval(timer);
  }, [view, loadRooms, tell]);
  useEffect(() => {
    if (view !== 'fourth_section') return undefined;
    const refreshBanner = () => loadHome().catch((e) => tell(`Banner refresh failed: ${e.message}`));
    refreshBanner();
    const timer = window.setInterval(refreshBanner, 1000);
    return () => window.clearInterval(timer);
  }, [view, loadHome, tell]);
  useEffect(() => {
    if (view !== 'fourth_section' || !isAdmin) return undefined;
    const refreshScheduledBanners = () => loadScheduledBanners()
      .catch((e) => tell(`Scheduled banners failed to load: ${e.message}`));
    refreshScheduledBanners();
    const timer = window.setInterval(refreshScheduledBanners, 5000);
    return () => window.clearInterval(timer);
  }, [view, isAdmin, loadScheduledBanners, tell]);
  useEffect(() => {
    if (!isAdmin) return;
    loadAdminDonorSlots().catch(() => {});
  }, [isAdmin, loadAdminDonorSlots]);
  useEffect(() => {
    const timer = window.setInterval(() => setTick(performance.now()), 1000);
    const handleExpired = () => { setIsAdmin(false); setUsername(''); setDashboardMode(false); tell('Admin session expired. Please sign in again.'); };
    window.addEventListener('cashff-auth-expired', handleExpired);
    return () => { window.clearInterval(timer); window.removeEventListener('cashff-auth-expired', handleExpired); };
  }, [tell]);
  useEffect(() => {
    const nextTransition = rooms.reduce((nearest, room) => {
      const milliseconds = ['Scheduled', 'Starting', 'Active'].includes(room.status)
        ? room.millisecondsUntilTransition : null;
      return milliseconds == null ? nearest : Math.min(nearest, milliseconds);
    }, Number.POSITIVE_INFINITY);
    if (!Number.isFinite(nextTransition)) return undefined;
    const timer = window.setTimeout(() => {
      loadRooms().catch((e) => tell(`Room refresh failed: ${e.message}`));
    }, Math.max(100, nextTransition + 100));
    return () => window.clearTimeout(timer);
  }, [rooms, loadRooms, tell]);

  const go = (next) => { setView(next); if (next !== 'support') setDashboardMode(false); };
  const goHome = () => go('home');
  const doLogin = async (event) => {
    event.preventDefault(); setLoginBusy(true); setLoginError('');
    try {
      const result = await api('/api/auth/login', { method: 'POST', body: loginForm });
      localStorage.setItem('cashff_token', result.token); localStorage.setItem('cashff_username', result.username);
      setIsAdmin(true); setUsername(result.username); setLoginOpen(false); setLoginForm({ username: '', password: '' });
      tell('Admin signed in successfully.');
    } catch (e) { setLoginError(e.message || 'Invalid username or password.'); }
    finally { setLoginBusy(false); }
  };
  const logout = () => {
    localStorage.removeItem('cashff_token'); localStorage.removeItem('cashff_username');
    setIsAdmin(false);
    setUsername('');
    setDashboardMode(false);
    setAllDonorSlots(EMPTY_HOME.donorSlots);
    setDonorModal(false);
    setDonorSlot(SLOTS[0]);
    setDonorDraft([]);
    setHome(EMPTY_HOME);
    setActiveSlot(SLOTS[0]);
    tell('Signed out of admin.');
    refreshPublic().catch(() => {});
  };
  const withBusy = async (action, successMessage) => {
    setBusy(true);
    try { const result = await action(); if (successMessage) tell(successMessage); return result; }
    catch (e) { tell(e.message || 'Something went wrong.'); return null; }
    finally { setBusy(false); }
  };

  const openDonorEditor = async () => {
    const donorData = await loadAdminDonorSlots();
    const slot = activeSlot || SLOTS[0];
    setDonorSlot(slot);
    setDonorDraft((donorData?.[slot] || []).map((x, i) => ({ rank: ['1st', '2nd', '3rd'][i], name: x.name || '', amount: x.amount || '', type: x.type || '', link: x.link || '' })));
    setDonorModal(true);
  };
  const changeDonorSlot = (slot) => {
    setDonorSlot(slot);
    setDonorDraft((allDonorSlots?.[slot] || []).map((x, i) => ({ rank: ['1st', '2nd', '3rd'][i], name: x.name || '', amount: x.amount || '', type: x.type || '', link: x.link || '' })));
  };
  const updateDonor = (index, key, value) => setDonorDraft((old) => { const copy = [...old]; copy[index] = { ...(copy[index] || { rank: ['1st', '2nd', '3rd'][index] }), [key]: value }; return copy; });
  const saveDonors = async () => withBusy(async () => {
    await api(`/api/admin/donor-slots/${encodeURIComponent(donorSlot)}`, { method: 'PUT', body: donorDraft });
    await loadAdminDonorSlots();
    await loadHome();
    setActiveSlot(donorSlot);
    setDonorModal(false);
  }, `Top supporters for ${donorSlot} updated.`);

  const publishCodes = async (event) => {
    event.preventDefault();
    if (!codesText.trim()) return tell('Please enter at least one redeem code.');
    if (!codeStartTime || !codeEndTime) return tell('Both start and end date-time fields are required.');
    const startAt = Date.parse(`${codeStartTime}:00+05:30`);
    const endAt = Date.parse(`${codeEndTime}:00+05:30`);
    if (Number.isNaN(startAt) || Number.isNaN(endAt)) return tell('Enter valid start and end date-time values.');
    if (startAt <= Date.now()) return tell('Start time cannot be in the past (IST).');
    if (endAt <= startAt) return tell('End time must be later than the start time (IST).');

    setBusy(true);
    try {
      const result = await api('/api/admin/redeem-codes', {
        method: 'POST',
        body: { codes: codesText, startAt: codeStartTime, endAt: codeEndTime },
      });
      if (!Number.isInteger(result?.count) || result.count < 1) {
        throw new Error('The server did not confirm that any redeem codes were saved.');
      }
      setCodesText('');
      setCodeStartTime('');
      setCodeEndTime('');

      let refreshMessage = '';
      try {
        await loadCodes();
      } catch (error) {
        refreshMessage = ` The published codes could not be refreshed: ${error.message}`;
      }
      tell(`${notifyCodes ? '🔔 New redeem codes are now published.' : 'Redeem codes published.'}${refreshMessage}`);
    } catch (error) {
      tell(error.message || 'Could not publish redeem codes.');
    } finally {
      setBusy(false);
    }
  };
  const deleteAllCodes = async () => {
    if (!window.confirm('Delete all redeem codes? This cannot be undone.')) return;
    await withBusy(async () => { await api('/api/admin/redeem-codes', { method: 'DELETE' }); await loadCodes(); }, 'All redeem codes deleted.');
  };
  const createRoom = async (event) => {
    event.preventDefault();
    if (!roomDraft.roomCode.trim()) return tell('Room ID is required.');
    if (!roomDraft.startAt || !roomDraft.endAt) return tell('Both start and end date/time fields are required.');
    const startMs = Date.parse(`${roomDraft.startAt}:00Z`);
    const endMs = Date.parse(`${roomDraft.endAt}:00Z`);
    if (Number.isNaN(startMs) || Number.isNaN(endMs)) return tell('Enter valid start and end date/time values.');
    if (endMs <= startMs + 180000) return tell('End time must be later than the start time plus three minutes.');
    const result = await withBusy(async () => {
      const item = legacyRoomToSchedule
        ? await api(`/api/admin/rooms/${legacyRoomToSchedule.id}/schedule`, {
          method: 'PUT',
          body: { startAt: roomDraft.startAt, endAt: roomDraft.endAt },
        })
        : await api('/api/admin/rooms', { method: 'POST', body: roomDraft });
      await loadRooms();
      setRoomDraft({ roomCode: '', passwordCode: '111', startAt: '', endAt: '' });
      setLegacyRoomToSchedule(null);
      return item;
    }, legacyRoomToSchedule
      ? 'Legacy room scheduled. The countdown begins at its start time.'
      : 'Custom room scheduled. The 3-minute timer begins at its start time.');
    if (result && notifyRooms && !legacyRoomToSchedule) tell('⚡ New custom room published.');
  };
  const scheduleLegacyRoom = (room) => {
    setLegacyRoomToSchedule(room);
    setRoomDraft({ roomCode: room.roomCode, passwordCode: room.passwordCode || '', startAt: '', endAt: '' });
  };
  const cancelLegacySchedule = () => {
    setLegacyRoomToSchedule(null);
    setRoomDraft({ roomCode: '', passwordCode: '111', startAt: '', endAt: '' });
  };
  const deleteRoom = async (id) => {
    if (!window.confirm('Delete this custom room?')) return;
    await withBusy(async () => { await api(`/api/admin/rooms/${id}`, { method: 'DELETE' }); await loadRooms(); }, 'Custom room deleted.');
  };
  const deleteAllRooms = async () => {
    if (!window.confirm('Delete all custom rooms?')) return;
    await withBusy(async () => { await api('/api/admin/rooms', { method: 'DELETE' }); await loadRooms(); }, 'All custom rooms deleted.');
  };
  const uploadWinners = async (event) => {
    event.preventDefault(); if (!winnerRoom || winnerFiles.some((f) => !f)) return tell('Please choose all three winner photos.');
    const data = new FormData(); winnerFiles.forEach((file, i) => data.append(`p${i + 1}`, file));
    const result = await withBusy(async () => { const r = await api(`/api/admin/rooms/${winnerRoom.id}/photos`, { method: 'POST', body: data }); await loadRooms(); setWinnerRoom(null); setWinnerFiles([null, null, null]); return r; }, 'Winner photos uploaded.');
    return result;
  };
  const uploadBanner = async (event) => {
    event.preventDefault();
    const formElement = event.currentTarget;
    if (!bannerFile) return tell('Please choose a banner image first.');
    if (!bannerStartTime || !bannerEndTime) return tell('Both banner start and end times are required.');
    const startAt = Date.parse(`${bannerStartTime}:00+05:30`);
    const endAt = Date.parse(`${bannerEndTime}:00+05:30`);
    if (Number.isNaN(startAt) || Number.isNaN(endAt)) return tell('Enter valid banner start and end times.');
    if (startAt < Date.now()) return tell('Banner start time cannot be in the past (IST).');
    if (endAt <= startAt) return tell('Banner end time must be later than its start time (IST).');

    setBusy(true);
    try {
      const body = new FormData();
      body.append('image', bannerFile);
      body.append('startAt', bannerStartTime);
      body.append('endAt', bannerEndTime);
      const result = await api('/api/admin/banners', { method: 'POST', body });
      if (!result?.id) throw new Error('The server did not confirm that the banner was scheduled.');
      formElement.reset();
      setBannerFile(null);
      setBannerStartTime('');
      setBannerEndTime('');
      let refreshMessage = '';
      try {
        await Promise.all([loadHome(), loadScheduledBanners()]);
      } catch (error) {
        refreshMessage = ` The scheduled banner could not be refreshed: ${error.message}`;
      }
      tell(`Banner scheduled successfully.${refreshMessage}`);
    } catch (error) {
      tell(error.message || 'Could not schedule banner.');
    } finally {
      setBusy(false);
    }
  };
  const deleteBanner = async (id) => {
    if (!window.confirm('Delete this scheduled banner?')) return;
    await withBusy(async () => {
      await api(`/api/admin/banners/${id}`, { method: 'DELETE' });
      await Promise.all([loadHome(), loadScheduledBanners()]);
    }, 'Scheduled banner deleted.');
  };
  const submitDonation = async (event) => {
    event.preventDefault();
    const formElement = event.currentTarget;
    if (!/^[0-9]{10}$/.test(donationForm.whatsapp)) return tell('Enter a valid 10-digit WhatsApp number.');
    if (!donationForm.amount || Number(donationForm.amount) < 1) return tell('Donation amount must be at least ₹1.');
    if (!donationForm.screenshot) return tell('Please upload your payment screenshot.');
    if (!donationForm.agree) return tell('Please accept the non-refundable donation policy.');
    const body = new FormData(); body.append('whatsapp', donationForm.whatsapp); body.append('amount', donationForm.amount); body.append('screenshot', donationForm.screenshot); body.append('agree', 'true');
    setDonationBusy(true);
    try { const result = await api('/api/public/support/donations', { method: 'POST', body }); tell(`Donation screenshot submitted. Recorded total for this number: ${money(result.totalAmount)}.`); setDonationForm({ whatsapp: '', amount: '', screenshot: null, agree: false }); formElement.reset(); }
    catch (e) { tell(e.message || 'Donation submission failed.'); }
    finally { setDonationBusy(false); }
  };
  const uploadQr = async (event) => {
    event.preventDefault();
    const formElement = event.currentTarget; if (!qrFile) return tell('Please choose a QR image first.');
    const body = new FormData(); body.append('image', qrFile);
    await withBusy(async () => { const result = await api('/api/admin/support/qr', { method: 'POST', body }); setQrUrl(result.qrUrl || ''); setQrFile(null); formElement.reset(); }, 'Merchant QR updated.');
  };
  const loadDonations = async (overrides = {}) => {
    const f = { ...donationFilters, ...overrides };
    const params = new URLSearchParams(); Object.entries(f).forEach(([k, v]) => { if (v) params.set(k, (k === 'startTime' || k === 'endTime') ? new Date(v).toISOString() : v); });
    await withBusy(async () => { setDonations(await api(`/api/admin/donations?${params.toString()}`)); setDashboardMode(true); }, null);
  };
  const deleteDonation = async (whatsapp) => {
    if (!window.confirm(`Delete the donation entry for ${whatsapp}?`)) return;
    await withBusy(async () => { await api(`/api/admin/donations/${encodeURIComponent(whatsapp)}`, { method: 'DELETE' }); await loadDonations(); }, 'Donation entry deleted.');
  };
  const openSupport = () => { go('support'); setDashboardMode(false); loadQr().catch(() => {}); };
  const remainingSeconds = (room) => Math.max(0, Math.ceil(Number(room.secondsRemaining || 0) - (tick - roomsReceivedAt) / 1000));
  const titleMap = { home: 'CASH FF ESPORTS', g_rcode: 'REDEEM CODES', custom: 'CUSTOM ROOMS', fourth_section: 'TODAY PHOTO / BANNER', support: dashboardMode ? 'LIVE DONATION DASHBOARD' : 'GAMING DONATION SUPPORT' };

  const publicDisplaySlot = !isAdmin && activeSlot && home?.activeSlot && activeSlot === home.activeSlot ? activeSlot : null;
  const publicSupporters = publicDisplaySlot ? (home.donorSlots?.[publicDisplaySlot] || []) : [];

  const Header = () => <header className="app-header sticky top-0 z-40 flex items-center justify-between border-b border-purple-500/40 bg-slate-950/95 px-4 py-4 backdrop-blur-md">
    <div className="flex min-w-0 items-center gap-2">
      {view !== 'home' && <button onClick={goHome} className="p-1 text-xl text-pink-400 hover:text-white" aria-label="Back home">‹</button>}
      <h1 className="truncate text-lg font-extrabold tracking-wider rgb-title">{titleMap[view] || 'CASH FF ESPORTS'}</h1>
    </div>
    <div className="ml-2 flex shrink-0 items-center gap-2">
      {isAdmin ? <><span className="hidden rounded-full border border-emerald-500/60 bg-emerald-950/60 px-2 py-1 text-[10px] font-bold uppercase tracking-wider text-emerald-300 sm:inline">ADMIN · {username}</span><button onClick={logout} className="rounded-lg border border-red-500/50 px-2 py-1.5 text-[10px] font-bold text-red-300 hover:bg-red-950/50">Logout</button></> : <button onClick={() => { setLoginError(''); setLoginOpen(true); }} className="rounded-full border border-purple-500/50 bg-slate-900/90 px-3 py-1.5 text-[10px] font-bold uppercase tracking-wider text-purple-300 hover:border-pink-400">Admin Login</button>}
    </div>
  </header>;

  return <div className="min-h-screen px-0 sm:flex sm:justify-center sm:px-4">
    <div className={`app-frame relative flex min-h-screen w-full flex-col overflow-hidden sm:min-h-[920px] ${view === 'support' ? 'bg-transparent shadow-none sm:rounded-none' : 'border-2 border-purple-900/80 bg-slate-950 shadow-2xl sm:rounded-3xl'}`}>
      {view !== 'support' && <Header />}
      <main className={`app-main flex-1 overflow-y-auto ${view === 'home' ? 'view-home' : `view-${view}`}`}>
        {view === 'home' && <>
          <section className="home-supporters rgb-card space-y-3 rounded-2xl p-3.5">
            <div className="flex items-center justify-between border-b border-purple-900/50 pb-2">
              <h2 className="flex items-center gap-1 text-xs font-bold uppercase tracking-wider text-pink-400"><span>🔥</span> Top Army Supporters</h2>
              <div className="flex items-center gap-1.5"><button onClick={() => setNoticeOpen(true)} className="rounded border border-cyan-500 bg-purple-900/80 px-2.5 py-1 text-[10px] font-bold text-cyan-300">📢 Notice</button>{isAdmin && <button onClick={openDonorEditor} className="rounded bg-amber-600 px-2.5 py-1 text-[10px] font-extrabold text-slate-950 hover:bg-amber-500">✏️ Edit</button>}</div>
            </div>
            <div className="flex gap-1.5 rounded-xl border border-purple-900/50 bg-slate-950 p-1">{SLOTS.map((slot) => <button key={slot} onClick={() => setActiveSlot(slot)} className={`flex-1 rounded-lg border py-1.5 text-xs transition ${activeSlot === slot ? 'border-cyan-400 bg-gradient-to-r from-pink-600 to-purple-600 font-bold text-white shadow-lg' : 'border-purple-900/60 bg-slate-900 text-slate-400 hover:text-white'}`}>{slot}</button>)}</div>
            <div className="space-y-2.5">{publicSupporters.length ? publicSupporters.map((d, i) => <div key={`${activeSlot}-${i}`} className="flex items-center justify-between rounded-xl border border-purple-500/40 bg-slate-900/90 p-3 shadow-md">
              <div className="min-w-0 space-y-1"><div className="flex flex-wrap items-center gap-1.5"><span className="rounded border border-pink-500 bg-purple-950 px-1.5 py-0.5 text-[10px] font-bold text-pink-300">{d.rank || ['1st','2nd','3rd'][i]}</span><span className="text-sm font-extrabold tracking-wide text-white">{d.name}</span><span className="rounded-md border border-emerald-500 bg-emerald-500/20 px-2 py-0.5 text-[10px] font-bold text-emerald-300">{d.amount}</span></div><div className="flex items-center gap-2 pl-1"><span className="rounded border border-cyan-800 bg-cyan-950 px-1.5 py-0.5 text-[9px] font-semibold uppercase text-cyan-300">{d.type}</span><a href={d.link} target="_blank" rel="noreferrer" className="block max-w-[160px] truncate font-mono text-[11px] text-pink-400 hover:underline">{d.link}</a></div></div><a href={d.link} target="_blank" rel="noreferrer" className="gaming-btn shrink-0 rounded-xl px-3 py-1.5 text-[11px] font-bold text-white">Visit</a>
            </div>) : <p className="py-5 text-center text-xs text-slate-500">No supporters published for this slot yet.</p>}</div>
            <button onClick={openSupport} className="gaming-btn mt-1 w-full rounded-xl py-2 text-xs font-bold uppercase tracking-wider text-white shadow-lg">❤️ VOLUNTEER SUPPORT NOW</button>
          </section>
          <section className="home-features mt-3 space-y-3">
            <NavCard icon="🎁" title="G. RCODE" subtitle="Google Play Redeem Codes Hub" onClick={() => go('g_rcode')} />
            <NavCard icon="⚡" title="CUSTAM" subtitle="Custom Room ID & 3-Min Blur Timer" onClick={() => go('custom')} />
            <div className="rgb-card flex w-full items-center justify-between rounded-2xl p-3.5 opacity-60"><div className="flex items-center gap-3"><span className="rounded-xl border border-slate-800 bg-slate-900 p-2 text-xl">⏳</span><div><span className="block text-sm text-white">NO Caption</span><span className="text-[11px] text-amber-400">Coming Soon</span></div></div><span className="text-cyan-400">→</span></div>
            <NavCard icon="🖼️" title="TODAY PHOTO / BANNER" subtitle="Special Daily Post / Banner" onClick={() => go('fourth_section')} />
          </section>
        </>}

        {view === 'g_rcode' && <div className="space-y-3">
          <div className="rgb-card mb-3 flex items-center justify-between rounded-xl p-3"><div className="flex items-center gap-2"><span className="text-base">🔔</span><div><span className="block text-xs font-bold text-white">{notifyCodes ? 'Notification Active' : 'Notification Off'}</span><span className="text-[10px] text-slate-400">नया कोड डलते ही तुरंत नोटिफाई किया जाएगा</span></div></div><Switch checked={notifyCodes} onChange={(value) => { setNotifyCodes(value); localStorage.setItem('cashff_notify_codes', String(value)); tell(value ? '🔔 Redeem-code notifications on.' : '🔕 Redeem-code notifications off.'); }} /></div>
          {isAdmin && <form onSubmit={publishCodes} className="rgb-card mb-4 space-y-2 rounded-2xl p-4"><h3 className="text-xs font-bold uppercase tracking-wider text-pink-400">Admin: Drop Redeem Codes</h3><textarea value={codesText} onChange={(e) => setCodesText(e.target.value)} placeholder="Paste codes line by line..." className="form-control h-24 resize-y"/><div className="grid grid-cols-1 gap-2 sm:grid-cols-2"><div><label className="form-label">Start Date & Time (IST)</label><input type="datetime-local" value={codeStartTime} onChange={(e) => setCodeStartTime(e.target.value)} className="form-control" /></div><div><label className="form-label">End Date & Time (IST)</label><input type="datetime-local" value={codeEndTime} onChange={(e) => setCodeEndTime(e.target.value)} className="form-control" /></div></div><button disabled={busy} className="gaming-btn w-full rounded-xl py-2 text-xs font-bold uppercase tracking-wider text-white">{busy ? 'Publishing…' : 'Publish Codes'}</button></form>}
          {isAdmin && codes.length > 0 && <div className="mb-3 flex items-center justify-between px-1"><span className="text-xs text-slate-400">Total Codes: {codes.length}</span><button onClick={deleteAllCodes} className="rounded-lg border border-red-900 bg-red-950/50 px-3 py-1 text-xs font-semibold text-red-400">All Delete</button></div>}
          {codes.length ? codes.map((item, i) => <div key={item.id} className="code-record rgb-card mb-3 flex items-center justify-between rounded-2xl p-3.5"><div className="min-w-0"><span className={`code-status status-${String(item.status || 'Active').toLowerCase()} mb-0.5 block text-[10px] font-bold text-cyan-300`}>#{codes.length - i} · {item.status || 'Active'} · {item.displayStart || item.startAt || 'Time pending'}</span><span className="block text-[10px] text-slate-400">{item.displayEnd || item.endAt || '—'}</span><span className="break-all font-mono text-sm font-bold tracking-wider text-white">{item.code}</span></div><button disabled={item.status !== 'Active'} onClick={async () => { try { await navigator.clipboard.writeText(item.code); tell('Redeem code copied.'); } catch { tell(item.code); } }} className={`ml-3 shrink-0 rounded-xl px-4 py-1.5 text-xs font-bold uppercase tracking-wider text-white ${item.status === 'Active' ? 'gaming-btn' : 'cursor-not-allowed bg-slate-700 text-slate-400 opacity-70'}`}>{item.status === 'Active' ? 'Copy' : item.status}</button></div>) : <p className="py-6 text-center text-xs text-slate-500">No codes available.</p>}
        </div>}

        {view === 'custom' && <div className="space-y-2">
          <div className="rgb-card mb-3 flex items-center justify-between rounded-xl p-3"><div className="flex items-center gap-2"><span className="text-base">⚡</span><div><span className="block text-xs font-bold text-white">{notifyRooms ? 'Notification Active' : 'Notification Off'}</span><span className="text-[10px] text-slate-400">नया कस्टम मैच आते ही तुरंत नोटिफाई किया जाएगा</span></div></div><Switch checked={notifyRooms} onChange={(value) => { setNotifyRooms(value); localStorage.setItem('cashff_notify_rooms', String(value)); tell(value ? '⚡ Custom-room notifications on.' : '🔕 Custom-room notifications off.'); }} /></div>
          {isAdmin && <form onSubmit={createRoom} className="rgb-card mb-4 min-w-0 space-y-2.5 rounded-2xl p-4">
            <h3 className="text-xs font-bold uppercase tracking-wider text-pink-400">{legacyRoomToSchedule ? 'Admin: Schedule Existing Room' : 'Admin: Add Custom Room'}</h3>
            <label className="block min-w-0"><span className="form-label">Room ID</span><input required readOnly={Boolean(legacyRoomToSchedule)} value={roomDraft.roomCode} onChange={(e) => setRoomDraft({ ...roomDraft, roomCode: e.target.value })} placeholder="e.g. 12457" className="form-control read-only:opacity-70"/></label>
            {!legacyRoomToSchedule && <label className="block min-w-0"><span className="form-label">Password</span><input value={roomDraft.passwordCode} onChange={(e) => setRoomDraft({ ...roomDraft, passwordCode: e.target.value })} placeholder="Room password" className="form-control"/></label>}
            <label className="block min-w-0"><span className="form-label">Start Date &amp; Time (IST)</span><input required type="datetime-local" value={roomDraft.startAt} onChange={(e) => setRoomDraft({ ...roomDraft, startAt: e.target.value })} className="form-control"/></label>
            <label className="block min-w-0"><span className="form-label">End Date &amp; Time (IST)</span><input required type="datetime-local" value={roomDraft.endAt} onChange={(e) => setRoomDraft({ ...roomDraft, endAt: e.target.value })} className="form-control"/></label>
            <button disabled={busy} className="gaming-btn w-full rounded-xl py-2 text-xs font-bold uppercase tracking-wider text-white">{busy ? 'Saving…' : legacyRoomToSchedule ? 'Save Room Schedule' : 'Publish Room (Start 3M Timer)'}</button>
            {legacyRoomToSchedule && <button type="button" onClick={cancelLegacySchedule} className="w-full rounded-xl border border-purple-800 py-2 text-xs font-bold text-slate-300">Cancel</button>}
          </form>}
          {isAdmin && rooms.length > 0 && <div className="mb-2 flex justify-end"><button onClick={deleteAllRooms} className="rounded-lg border border-red-900 bg-red-950/50 px-3 py-1 text-xs font-semibold text-red-400">Delete All Rooms</button></div>}
          {rooms.length ? rooms.map((room, index) => {
            const secs = remainingSeconds(room); const hasPhotos = room.photos?.length > 0;
            const statusText = room.status === 'Scheduled' ? `Scheduled · ${room.displayStart}`
              : room.status === 'Starting' ? 'Starting · 3-minute countdown'
                : room.status === 'Active' ? `Active · Ends ${room.displayEnd}`
                  : room.status === 'Expired' ? 'Expired'
                    : 'Legacy room · no saved schedule';
            return <div key={room.id} className="rgb-card mb-3 space-y-2.5 rounded-2xl p-4">
              <div className="flex flex-wrap items-center justify-between gap-2 border-b border-purple-900/40 pb-2 text-xs font-medium text-slate-300">
                <span className="font-bold text-cyan-300">#{rooms.length - index}{room.passwordCode && (isAdmin || room.canCopy) ? ` | Pass: ${room.passwordCode}` : ''}</span>
                <div className="flex items-center gap-2">
                  <span className="text-right text-pink-400">{statusText}</span>
                  {isAdmin && <button onClick={() => deleteRoom(room.id)} className="rounded border border-red-900 bg-red-950/50 px-2 py-0.5 text-[10px] text-red-400">Delete</button>}
                  {isAdmin && room.status === 'Legacy' && <button onClick={() => scheduleLegacyRoom(room)} className="rounded border border-cyan-800 bg-cyan-950/50 px-2 py-0.5 text-[10px] text-cyan-300">Schedule</button>}
                </div>
              </div>
              <div className="flex flex-wrap items-center justify-between gap-2 rounded-xl border border-purple-900/50 bg-slate-900/90 p-3">
                <span className="flex items-center gap-2 font-mono text-sm font-extrabold text-white">ID: <span className={`relative rounded px-2 py-0.5 ${room.canCopy ? 'text-purple-300' : 'select-none bg-purple-950 text-purple-300 blur-sm'}`}>{room.canCopy ? room.roomCode : '•••••'}</span></span>
                <div className="flex items-center gap-2">
                  {hasPhotos ? <button onClick={() => setPhotosToView(room.photos)} className="gaming-btn rounded-xl px-4 py-1.5 text-xs font-bold uppercase tracking-wider text-white">C</button> : room.canCopy
                    ? <button onClick={() => copyRoom(room.roomCode, tell)} className="gaming-btn rounded-xl px-3.5 py-1.5 text-xs font-bold uppercase tracking-wider text-white">COPY ID</button>
                    : <button disabled className="cursor-not-allowed rounded-xl border border-purple-800 bg-purple-950/80 px-3.5 py-1.5 font-mono text-xs text-pink-400">{room.status === 'Scheduled' ? `STARTS IN ${formatCountdown(secs)}` : room.status === 'Starting' ? `⏳ ${formatCountdown(secs)}` : room.status}</button>}
                  {isAdmin && !hasPhotos && <button onClick={() => { setWinnerRoom(room); setWinnerFiles([null, null, null]); }} className="rounded-xl bg-amber-600 px-2 py-1.5 text-[10px] font-bold text-slate-950 hover:bg-amber-500">📷 C-Upload</button>}
                </div>
              </div>
              {isAdmin && hasPhotos && <button onClick={() => { setWinnerRoom(room); setWinnerFiles([null, null, null]); }} className="text-[10px] text-amber-300 underline">Replace winner photos</button>}
            </div>;
          }) : <p className="py-6 text-center text-xs text-slate-500">No custom rooms scheduled or active.</p>}
        </div>}

        {view === 'fourth_section' && <div className="space-y-4">
          {isAdmin && <form onSubmit={uploadBanner} className="rgb-card space-y-3 rounded-2xl p-4"><h3 className="text-xs font-bold uppercase tracking-wider text-pink-400">Admin: Schedule Banner (IST)</h3><input type="file" accept="image/*" onChange={(e) => setBannerFile(e.target.files?.[0] || null)} className="w-full cursor-pointer text-xs text-slate-300 file:mr-3 file:rounded-xl file:border-0 file:bg-purple-600 file:px-4 file:py-2 file:text-xs file:font-bold file:text-white"/><div className="grid grid-cols-1 gap-2 sm:grid-cols-2"><label><span className="form-label">Start Date &amp; Time (IST)</span><input required type="datetime-local" value={bannerStartTime} onChange={(e) => setBannerStartTime(e.target.value)} className="form-control"/></label><label><span className="form-label">End Date &amp; Time (IST)</span><input required type="datetime-local" value={bannerEndTime} onChange={(e) => setBannerEndTime(e.target.value)} className="form-control"/></label></div><button disabled={busy || !bannerFile} className="gaming-btn w-full rounded-xl py-2 text-xs font-bold uppercase tracking-wider text-white">{busy ? 'Scheduling…' : 'Schedule Banner'}</button></form>}
          {home.bannerUrl ? <div className="rgb-card space-y-2 rounded-2xl p-3.5 text-center"><span className="block text-xs font-bold text-cyan-300">Live Banner:</span><img src={fileUrl(home.bannerUrl)} alt="Live scheduled banner" className="max-h-96 w-full rounded-xl border border-purple-900 object-contain shadow-lg"/></div> : <div className="rgb-card rounded-2xl p-8 text-center text-xs text-slate-400">No banner is live right now.</div>}
          {isAdmin && <div className="space-y-3"><h3 className="px-1 text-xs font-bold uppercase tracking-wider text-pink-400">Scheduled Banners</h3>{scheduledBanners.length ? scheduledBanners.map((banner) => { const now = Date.now(); const start = Date.parse(banner.startAt); const end = Date.parse(banner.endAt); const status = now < start ? 'Upcoming' : now < end ? 'Live' : 'Expired'; return <div key={banner.id} className="rgb-card flex items-center gap-3 rounded-2xl p-3"><img src={fileUrl(banner.imageUrl)} alt="" className="h-16 w-24 rounded-lg border border-purple-900 object-cover"/><div className="min-w-0 flex-1 text-xs"><span className={`mb-1 block font-bold ${status === 'Live' ? 'text-emerald-300' : 'text-cyan-300'}`}>{status}</span><span className="block text-slate-300">Starts: {banner.displayStart}</span><span className="block text-slate-400">Ends: {banner.displayEnd}</span></div><button disabled={busy} onClick={() => deleteBanner(banner.id)} className="rounded border border-red-900 bg-red-950/50 px-3 py-1 text-xs font-semibold text-red-400">Delete</button></div>; }) : <p className="rgb-card rounded-xl p-4 text-center text-xs text-slate-400">No banners scheduled.</p>}</div>}
        </div>}

        {view === 'support' && <div className="support-shell">
          {!dashboardMode ? <>
            <div className="mb-5 flex flex-wrap items-center justify-between gap-3 border-b-2 border-slate-800 pb-4"><div className="flex min-w-0 items-center gap-3"><button onClick={goHome} className="shrink-0 rounded-lg border border-purple-700 px-2 py-2 text-xs font-bold text-purple-300 hover:border-pink-400">← Cash FF</button><h2 className="text-2xl font-extrabold text-cyan-300 [text-shadow:0_0_10px_rgba(0,255,255,.3)]">🔥 Gaming Donation Support</h2></div><div className="flex gap-2">{isAdmin ? <button onClick={() => loadDonations()} className="rounded-lg bg-gradient-to-r from-blue-600 to-cyan-500 px-3 py-2 text-xs font-bold text-white shadow-[0_0_10px_rgba(0,114,255,.4)]">📊 Dashboard</button> : <button onClick={() => { setLoginError(''); setLoginOpen(true); }} className="rounded-lg bg-gradient-to-r from-blue-600 to-cyan-500 px-3 py-2 text-xs font-bold text-white">🔐 Admin Login</button>}{isAdmin && <button onClick={logout} className="rounded-lg border border-red-500/50 px-3 py-2 text-xs font-bold text-red-300">Logout</button>}</div></div>
            <div className="mb-5 rounded-xl border-2 border-dashed border-cyan-400 bg-slate-800 p-4 text-center"><p className="mb-2 text-[13px] text-cyan-300"><strong>Scan to Pay (Click QR to Enlarge):</strong></p>{qrUrl ? <img src={fileUrl(qrUrl)} onClick={() => setImageViewer({ title: 'Merchant QR Code', urls: [qrUrl] })} className="mx-auto h-[130px] w-[130px] cursor-pointer rounded-lg border-2 border-pink-500 bg-white object-contain transition hover:scale-105" alt="Merchant QR code"/> : <div className="mx-auto flex h-[130px] w-[130px] items-center justify-center rounded-lg border border-purple-700 bg-slate-900 px-3 text-center text-xs text-slate-400">Payment QR not set yet.</div>}</div>
            <form onSubmit={submitDonation} className="space-y-4"><div><label className="form-label !text-sm !text-slate-300">1. Enter WhatsApp Number</label><input value={donationForm.whatsapp} onChange={(e) => setDonationForm({ ...donationForm, whatsapp: e.target.value })} inputMode="numeric" maxLength={10} pattern="[0-9]{10}" placeholder="e.g. 9876543210" required className="form-control !rounded-lg !border-slate-700 !bg-slate-800 !p-3"/></div><div><label className="form-label !text-sm !text-slate-300">2. Enter Amount (₹)</label><input value={donationForm.amount} onChange={(e) => setDonationForm({ ...donationForm, amount: e.target.value })} type="number" min="1" step="0.01" placeholder="e.g. 50 or 100" required className="form-control !rounded-lg !border-slate-700 !bg-slate-800 !p-3"/></div><div className="rounded-xl border-2 border-cyan-400 bg-slate-800 p-3 text-left"><label className="form-label !text-sm !text-slate-300">3. Upload Payment Screenshot</label><input type="file" accept="image/*" required onChange={(e) => setDonationForm({ ...donationForm, screenshot: e.target.files?.[0] || null })} className="w-full text-xs text-slate-300 file:mr-2 file:rounded-lg file:border-0 file:bg-purple-600 file:px-3 file:py-2 file:text-xs file:font-bold file:text-white"/></div><label className="flex items-center gap-2 text-xs text-slate-100"><input type="checkbox" checked={donationForm.agree} onChange={(e) => setDonationForm({ ...donationForm, agree: e.target.checked })} required className="accent-pink-500"/>I agree that this donation is non-refundable.</label><button disabled={donationBusy} className="gaming-btn w-full rounded-lg py-3 text-sm font-bold text-white">{donationBusy ? 'Submitting…' : '🚀 Share / Submit Donation'}</button></form>
          </> : <>
            <div className="mb-5 flex items-center justify-between border-b-2 border-slate-800 pb-4"><h2 className="text-xl font-extrabold text-cyan-300">📈 Live Donation Dashboard</h2><button onClick={() => setDashboardMode(false)} className="gaming-btn rounded-lg px-3 py-2 text-xs font-bold text-white">🏠 Back to Form</button></div>
            <div className="mb-5 rounded-xl border border-cyan-400 bg-slate-800 p-4"><label className="mb-2 block text-[13px] font-bold text-cyan-300">⚙️ Admin Panel: Update Merchant QR Code</label><form onSubmit={uploadQr} className="flex flex-col gap-2 sm:flex-row"><input type="file" accept="image/*" onChange={(e) => setQrFile(e.target.files?.[0] || null)} className="min-w-0 flex-1 text-xs text-slate-300 file:rounded file:border-0 file:bg-slate-700 file:px-2 file:py-1"/><button disabled={!qrFile || busy} className="gaming-btn rounded-lg px-3 py-2 text-xs font-bold text-white">Save QR</button></form></div>
            <form onSubmit={(e) => { e.preventDefault(); loadDonations(); }} className="mb-5 grid grid-cols-2 gap-2 rounded-xl border border-slate-700 bg-slate-800 p-3"><div><label className="form-label">Start Time:</label><input type="datetime-local" value={donationFilters.startTime} onChange={(e) => setDonationFilters({ ...donationFilters, startTime: e.target.value })} className="form-control !rounded-lg !border-slate-700 !bg-slate-900 !p-2"/></div><div><label className="form-label">End Time:</label><input type="datetime-local" value={donationFilters.endTime} onChange={(e) => setDonationFilters({ ...donationFilters, endTime: e.target.value })} className="form-control !rounded-lg !border-slate-700 !bg-slate-900 !p-2"/></div><div className="col-span-2 mt-1 flex items-center justify-between gap-2"><div className="flex min-w-0 items-center gap-2"><label className="shrink-0 text-xs text-slate-300">Sort By:</label><select value={donationFilters.sortBy} onChange={(e) => setDonationFilters({ ...donationFilters, sortBy: e.target.value })} className="form-control !w-auto !rounded-lg !border-slate-700 !bg-slate-900 !p-1.5"><option value="latest">Latest First</option><option value="highToLow">High to Low Amount</option><option value="lowToHigh">Low to High Amount</option></select></div><button type="button" onClick={() => { const f = { startTime: '', endTime: '', sortBy: 'latest' }; setDonationFilters(f); loadDonations(f); }} className="shrink-0 rounded-lg bg-gradient-to-r from-blue-600 to-cyan-500 px-3 py-1.5 text-xs font-bold text-white">Clear Filters</button></div></form>
            <div className="space-y-3">{donations.length ? donations.map((d) => <div key={d.whatsapp} className="flex items-center justify-between gap-3 rounded-xl border border-slate-700 bg-slate-800 p-3 shadow-md"><div className="min-w-0"><span className="block break-all text-base font-bold text-slate-100">📱 {d.whatsapp}</span><span className="block text-base font-bold text-green-400">{money(d.amount)}</span><span className="block text-xs text-slate-400">🕒 {new Date(d.rawTime).toLocaleString()}</span></div><div className="flex shrink-0 flex-col items-end gap-2"><button onClick={() => setImageViewer({ title: `WhatsApp: ${d.whatsapp} (${d.screenshots?.length || 0} Screenshot(s))`, urls: (d.screenshots || []).map((s) => s.url) })} className="rounded-md bg-gradient-to-r from-amber-500 to-amber-600 px-3 py-2 text-xs font-bold text-white">📷 View ({d.screenshots?.length || 0})</button><button onClick={() => deleteDonation(d.whatsapp)} className="rounded-md bg-gradient-to-r from-red-500 to-red-600 px-3 py-1.5 text-xs font-bold text-white">🗑️ Delete</button></div></div>) : <div className="py-8 text-center text-sm text-slate-400">No donation records found for the selected filters.</div>}</div>
          </>}
        </div>}
      </main>
      {view !== 'support' && <footer className="border-t border-purple-900/40 bg-slate-950 px-4 py-3 text-center text-[11px] uppercase tracking-widest text-slate-400">🔥 Free Fire Custom & Redeem Platform © 2026</footer>}
    </div>

    {flash && <div role="status" className="fixed bottom-4 left-1/2 z-[120] w-[calc(100%-32px)] max-w-md -translate-x-1/2 rounded-xl border border-cyan-400/60 bg-slate-900/95 px-4 py-3 text-sm text-white shadow-2xl">{flash}</div>}

    {loginOpen && <Modal title="🔐 Admin Login" onClose={() => setLoginOpen(false)}><form onSubmit={doLogin} className="space-y-3"><p className="text-xs text-slate-400">Sign in to manage redeem codes, rooms, supporters, banners, and donations.</p><div><label className="form-label">Username</label><input autoComplete="username" required value={loginForm.username} onChange={(e) => setLoginForm({ ...loginForm, username: e.target.value })} className="form-control" placeholder="Admin username"/></div><div><label className="form-label">Password</label><input type="password" autoComplete="current-password" required value={loginForm.password} onChange={(e) => setLoginForm({ ...loginForm, password: e.target.value })} className="form-control" placeholder="Admin password"/></div>{loginError && <p className="rounded-lg border border-red-500/40 bg-red-950/40 p-2 text-xs text-red-300">{loginError}</p>}<button disabled={loginBusy} className="gaming-btn w-full rounded-xl py-2.5 text-sm font-bold text-white">{loginBusy ? 'Signing in…' : 'Sign In'}</button></form></Modal>}

    {noticeOpen && <Modal title="📢 Notice & Rules" onClose={() => setNoticeOpen(false)}><div className="space-y-3 text-xs leading-relaxed text-slate-300"><div className="space-y-1.5 rounded-xl border border-purple-900/60 bg-slate-900/90 p-3"><p className="font-bold text-pink-400">⚠️ महत्वपूर्ण सूचना (Payment Policy):</p><p>आप जो भी पेमेंट कर रहे हैं, वह पूरी तरह से आपकी अपनी इच्छा और समर्थन पर है।</p></div><div className="space-y-1.5 rounded-xl border border-purple-900/60 bg-slate-900/90 p-3"><p className="font-bold text-cyan-400">🕒 टॉप सपोर्टर्स टाइम स्लॉट नियम:</p><p>तीनों टाइम स्लॉट (9:00 AM, 5:00 PM, 9:00 PM) के लिए ठीक उसके 30 मिनट पहले का डेटा कैप्चर किया जाता है।</p></div><button onClick={() => setNoticeOpen(false)} className="gaming-btn w-full rounded-xl py-2 text-xs font-bold uppercase tracking-wider text-white">समझ गया (Got It)</button></div></Modal>}

    {donorModal && <Modal title="⭐ Edit Top Supporters (Time Slots)" onClose={() => setDonorModal(false)}><div className="space-y-3"><div><label className="form-label">Select Slot to Update:</label><select value={donorSlot} onChange={(e) => changeDonorSlot(e.target.value)} className="form-control">{SLOTS.map((s) => <option key={s}>{s}</option>)}</select></div>{[0,1,2].map((i) => <div key={i} className="space-y-2 rounded-xl border border-purple-900 bg-slate-900 p-3"><span className="block text-xs font-bold text-pink-400">{['1st','2nd','3rd'][i]} Supporter</span><input value={donorDraft[i]?.name || ''} onChange={(e) => updateDonor(i, 'name', e.target.value)} placeholder="Name" className="form-control"/><div className="grid grid-cols-2 gap-2"><input value={donorDraft[i]?.amount || ''} onChange={(e) => updateDonor(i, 'amount', e.target.value)} placeholder="Amount (e.g. ₹500)" className="form-control"/><input value={donorDraft[i]?.type || ''} onChange={(e) => updateDonor(i, 'type', e.target.value)} placeholder="Type (e.g. YouTube)" className="form-control"/></div><input value={donorDraft[i]?.link || ''} onChange={(e) => updateDonor(i, 'link', e.target.value)} placeholder="URL Link" className="form-control"/></div>)}<button disabled={busy} onClick={saveDonors} className="gaming-btn w-full rounded-xl py-2.5 text-xs font-bold uppercase tracking-wider text-white">Save Slot Donors</button></div></Modal>}

    {winnerRoom && <Modal title="📷 Upload 3 Winner Photos" onClose={() => setWinnerRoom(null)}><form onSubmit={uploadWinners} className="space-y-3">{['1st Place Photo', '2nd Place Photo', '3rd Place Photo'].map((title, i) => <div key={title}><label className="form-label">{i + 1}. {title}</label><input type="file" accept="image/*" required onChange={(e) => setWinnerFiles((old) => old.map((f, index) => index === i ? e.target.files?.[0] || null : f))} className="w-full text-xs text-slate-300 file:rounded-lg file:border-0 file:bg-purple-600 file:px-3 file:py-1.5 file:text-xs file:font-bold file:text-white"/></div>)}<button disabled={busy} className="gaming-btn w-full rounded-xl py-2 text-xs font-bold uppercase tracking-wider text-white">Save & Publish 'C'</button></form></Modal>}

    {photosToView && <Modal title="🏆 Winner Screenshots (1st, 2nd, 3rd)" onClose={() => setPhotosToView(null)}><div className="max-h-[70vh] space-y-4 overflow-y-auto">{photosToView.map((url, i) => <div key={url} className="space-y-1"><span className="block text-[11px] font-bold text-pink-400">{['1st Place Winner','2nd Place Winner','3rd Place Winner'][i]}</span><img src={fileUrl(url)} alt={`${i + 1}${['st','nd','rd'][i]} place winner`} className="max-h-64 w-full rounded-xl border border-purple-900 object-contain"/></div>)}</div><button onClick={() => setPhotosToView(null)} className="gaming-btn mt-4 w-full rounded-xl py-2 text-xs font-bold uppercase tracking-wider text-white">Close</button></Modal>}

    {imageViewer && <Modal title={imageViewer.title} onClose={() => setImageViewer(null)}><div className="max-h-[70vh] space-y-4 overflow-y-auto">{imageViewer.urls.length ? imageViewer.urls.map((url, i) => <img key={`${url}-${i}`} src={fileUrl(url)} alt={`Uploaded screenshot ${i + 1}`} className="mt-2 w-full rounded-lg border border-slate-700 object-contain"/>) : <p className="py-6 text-sm text-slate-400">No screenshots attached.</p>}</div></Modal>}
  </div>;
}

function NavCard({ icon, title, subtitle, onClick }) {
  return <button onClick={onClick} className="rgb-card group flex w-full items-center justify-between rounded-2xl p-3.5 text-left transition"><div className="flex items-center gap-3"><span className="rounded-xl border border-purple-800 bg-purple-950/80 p-2 text-xl">{icon}</span><div><span className="block text-sm font-bold text-white transition group-hover:text-pink-400">{title}</span><span className="text-[11px] font-normal text-slate-400">{subtitle}</span></div></div><span className="text-lg text-cyan-400">→</span></button>;
}
function Switch({ checked, onChange }) { return <button type="button" role="switch" aria-checked={checked} onClick={() => onChange(!checked)} className={`relative h-6 w-11 shrink-0 rounded-full border transition ${checked ? 'border-pink-500 bg-gradient-to-r from-pink-600 to-purple-600' : 'border-purple-900 bg-slate-800'}`}><span className={`absolute top-[2px] h-[18px] w-[18px] rounded-full bg-white shadow transition ${checked ? 'left-[21px]' : 'left-[2px]'}`}/></button>; }
function Modal({ title, onClose, children }) { return <div className="modal-backdrop" role="presentation" onMouseDown={(e) => { if (e.target === e.currentTarget) onClose?.(); }}><section role="dialog" aria-modal="true" className="modal-panel"><div className="mb-3 flex items-center justify-between border-b border-purple-900/50 pb-2"><h3 className="pr-4 text-sm font-bold uppercase text-cyan-300">{title}</h3><button onClick={onClose} aria-label="Close" className="text-2xl font-bold leading-none text-pink-400">×</button></div>{children}</section></div>; }
async function copyRoom(roomCode, tell) { try { await navigator.clipboard.writeText(roomCode); tell('Room ID copied.'); } catch { tell(`Room ID: ${roomCode}`); } }
