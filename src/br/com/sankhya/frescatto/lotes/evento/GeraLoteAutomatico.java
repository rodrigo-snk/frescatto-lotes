package br.com.sankhya.frescatto.lotes.evento;

import br.com.sankhya.extensions.eventoprogramavel.EventoProgramavelJava;
import br.com.sankhya.frescatto.lotes.model.Lote;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.event.PersistenceEvent;
import br.com.sankhya.jape.event.TransactionContext;
import br.com.sankhya.jape.vo.DynamicVO;
import br.com.sankhya.mgecomercial.model.centrais.cac.CACSP;
import br.com.sankhya.modelcore.MGEModelException;
import br.com.sankhya.modelcore.comercial.EstoqueHelpper;
import br.com.sankhya.modelcore.comercial.centrais.CACHelper;
import br.com.sankhya.modelcore.comercial.util.LoteAutomaticoHelper;
import br.com.sankhya.modelcore.comercial.util.LoteInfoUtil;
import br.com.sankhya.modelcore.dwfdata.vo.CabecalhoNotaVO;
import br.com.sankhya.modelcore.dwfdata.vo.ItemNotaVO;
import br.com.sankhya.modelcore.util.DynamicEntityNames;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.timloteamento.model.lotes.GeracaoLotesHelper;

import java.math.BigDecimal;

import static br.com.sankhya.frescatto.lotes.model.Lote.*;

public class GeraLoteAutomatico implements EventoProgramavelJava {


    @Override
    public void beforeInsert(PersistenceEvent persistenceEvent) throws Exception {

    }

    @Override
    public void beforeUpdate(PersistenceEvent persistenceEvent) throws Exception {
        ItemNotaVO itemNotaVO = (ItemNotaVO) ((DynamicVO) persistenceEvent.getVo()).wrapInterface(ItemNotaVO.class);
        ItemNotaVO itemNotaOldVO = (ItemNotaVO) ((DynamicVO) persistenceEvent.getOldVO()).wrapInterface(ItemNotaVO.class);
        final boolean topTemLoteAutomatico = "S".equals(itemNotaVO.asString("CabecalhoNota.TipoOperacao.AD_LOTEAUT"));

        if (topTemLoteAutomatico && persistenceEvent.getModifingFields().isModifing("CONTROLE")){
            removeEstoque(itemNotaOldVO);
        }



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

        ItemNotaVO itemNotaVO = (ItemNotaVO) ((DynamicVO) persistenceEvent.getVo()).wrapInterface(ItemNotaVO.class);
        CabecalhoNotaVO cabVO = (CabecalhoNotaVO) dwfFacade.findEntityByPrimaryKeyAsVO(DynamicEntityNames.CABECALHO_NOTA, itemNotaVO.asBigDecimalOrZero("NUNOTA"), CabecalhoNotaVO.class);

        Lote.geraLotes(itemNotaVO,cabVO);

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
