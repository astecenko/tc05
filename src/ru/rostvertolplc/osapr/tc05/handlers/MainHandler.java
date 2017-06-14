package ru.rostvertolplc.osapr.tc05.handlers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.*;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.util.MessageBox;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * 
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class MainHandler extends AbstractHandler {
	/**
	 * The constructor.
	 */
	public MainHandler() {
	}

	/**
	 * the command has been executed, so extract extract the needed information
	 * from the application context.
	 * 
	 * @throws TCException
	 */

	boolean fillNode(TCComponentBOMLine bomLine1){
		try {
			AIFComponentContext[] arrayOfAIFComponentContext = null;
			Map<TCComponentItemRevision, Integer> map1 = new HashMap<TCComponentItemRevision, Integer>();
			try {
				arrayOfAIFComponentContext = bomLine1.getChildren();
			} catch (TCException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (arrayOfAIFComponentContext != null) {
				int k = 0;
				int i = arrayOfAIFComponentContext.length;
				TCComponentBOMLine bomLine2;
				for (int j = 0; j < i; j++) {
					bomLine2 = (TCComponentBOMLine) arrayOfAIFComponentContext[j]
							.getComponent();
					try {
						if (map1.containsKey(bomLine2.getItemRevision())) {
							setFindNo(bomLine2, map1.get(
									bomLine2.getItemRevision()).toString());
						} else {
							k++;
							map1.put(bomLine2.getItemRevision(), k);
							setFindNo(bomLine2, ((Integer) k).toString());
						}
					} catch (TCException e) {
						// TODO 
						e.printStackTrace();
						return false;
					}
					if (bomLine2.hasChildren()) {
						if (!fillNode(bomLine2))
							return false;
					}

				}
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	boolean setFindNo(TCComponentBOMLine bomLine1, String value1)
			throws TCException {
		try {
			bomLine1.lock();
			bomLine1.setProperty("bl_sequence_no", value1);
			if (bomLine1.getProperty("bl_quantity").equals(""))
				bomLine1.setProperty("bl_quantity", "1");
			bomLine1.save();
			bomLine1.unlock();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		//IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		AbstractAIFUIApplication currentApplication = AIFUtility
				.getCurrentApplication();
		// TCSession session = (TCSession)currentApplication.getSession();
		// AIFComponentContext context = currentApplication.getTargetContext();

		//InterfaceAIFComponent[] c_targets = AIFUtility.getTargetComponents();
		TCComponentBOMLine localTCComponentBOMLine;
		//InterfaceAIFComponent c_target = null;

		//AIFComponentContext[] arrayOfAIFComponentContext = null; // =
		// currentApplication.getTargetContexts();
		AIFComponentContext context1 = currentApplication.getTargetContext();

		if (context1 == null) {
			MessageBox.post("Сборка не выбрана!", "Teamcenter Error", MessageBox.ERROR);
			return null;
		}

		try {
			localTCComponentBOMLine = (TCComponentBOMLine) context1
					.getComponent();
		} catch (ClassCastException localClassCastException) {
			MessageBox.post(
					"Выбранный объект не является элементом структуры изделия",
					"Teamcenter Error", MessageBox.ERROR);
			return null;
		}
	
		if (fillNode(localTCComponentBOMLine)) {
			MessageBox.post(
					"Позиции успешно расставлены",
					"Teamcenter", MessageBox.INFORMATION);			
		} else {
			MessageBox.post(
					"При расстановке позиций произошла ошибка",
					"Teamcenter", MessageBox.ERROR);	
		}					
		return null;
	}
}