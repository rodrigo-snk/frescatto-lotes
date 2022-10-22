package br.com.sankhya.frescatto.lotes.evento;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.frescatto.lotes.model.Lote;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.modelcore.dwfdata.vo.CabecalhoNotaVO;
import br.com.sankhya.modelcore.dwfdata.vo.ItemNotaVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;

import static br.com.sankhya.frescatto.lotes.model.Lote.removeEstoque;

public class GeraLoteAutomaticoVar implements EventoProgramavelJava {


    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeDelete(PersistenceEvent persistenceEvent) throws Exception {

        ItemNotaVO itemNotaVO = (ItemNotaVO) ((DynamicVO) persistenceEvent.getVo()).wrapInterface(ItemNotaVO.class);
        final boolean topTemLoteAutomatico = "S".equals(itemNotaVO.asString("CabecalhoNota.TipoOperacao.AD_LOTEAUT"));

        if (topTemLoteAutomatico){
            removeEstoque(itemNotaVO);
        }

    }

    @Override
    public void afterInsert(PersistenceEvent persistenceEvent) throws Exception {
        EntityFacade dwfFacade = EntityFacadeFactory.getDWFFacade();

        DynamicVO varVO = (DynamicVO) persistenceEvent.getVo();
        CabecalhoNotaVO cabVO = (CabecalhoNotaVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, varVO.asBigDecimalOrZero("NUNOTA"), CabecalhoNotaVO.class);

        ItemNotaVO itemNotaVO = (ItemNotaVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.ITEM_NOTA, new Object[] {varVO.asBigDecimal("NUNOTA"), varVO.asBigDecimal("SEQUENCIA")}, ItemNotaVO.class);

        Lote.geraLotes(itemNotaVO,cabVO, varVO);

    }

    @Override
    public void afterUpdate(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void afterDelete(PersistenceEvent persistenceEvent) throws Exception {


    }

    @Override
    public void beforeCommit(TransactionContext transactionContext) throws Exception {

    }
}
